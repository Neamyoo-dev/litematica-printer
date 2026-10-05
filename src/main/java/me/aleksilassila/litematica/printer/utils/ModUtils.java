package me.aleksilassila.litematica.printer.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class ModUtils {
    // 阻止 UI 显示 如果此时已经在 UI 中 请设置为 2 因为关闭 UI 也会调用一次
    public static int closeScreen = 0;

    public static boolean isLoadMod(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isBedrockMinerLoaded() {
        return isLoadMod("bedrockminer");
    }

    public static boolean isBlockMinerLoaded() {
        return isLoadMod("blockminer");
    }

    public static boolean isTweakerooLoaded() {
        return isLoadMod("tweakeroo");
    }

    public static boolean isRemoteInventoryNextLoaded() {
        return isLoadMod("remote-inventory-next");
    }

    public static boolean isQuickShulkerLoaded() {
        return isLoadMod("quickshulker");
    }

    public static boolean isTakeItOutLoaded() {
        return isLoadMod("takeitout");
    }

    private static @Nullable Object tweakToolSwitchEnum;
    private static @Nullable Method trySwitchToEffectiveToolMethod;
    private static @Nullable Method getBooleanValueMethod;

    static {
        if (FabricLoader.getInstance().isModLoaded("tweakeroo")) {
            try {
                Class<?> featureToggleClass = Class.forName("fi.dy.masa.tweakeroo.config.FeatureToggle");
                tweakToolSwitchEnum = featureToggleClass.getField("TWEAK_TOOL_SWITCH").get(null);

                Class<?> iConfigBooleanClass = Class.forName("fi.dy.masa.malilib.config.IConfigBoolean");
                getBooleanValueMethod = iConfigBooleanClass.getDeclaredMethod("getBooleanValue");

                Class<?> inventoryUtilsClass = Class.forName("fi.dy.masa.tweakeroo.util.InventoryUtils");
                trySwitchToEffectiveToolMethod = inventoryUtilsClass.getDeclaredMethod("trySwitchToEffectiveTool", BlockPos.class);

            } catch (Exception e) {
                tweakToolSwitchEnum = null;
                trySwitchToEffectiveToolMethod = null;
                getBooleanValueMethod = null;
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查 Tweakeroo 的 TWEAK_TOOL_SWITCH 选项是否启用。
     * @return 如果 Tweakeroo 存在且选项启用，则返回 true，否则返回 false。
     */
    public static boolean isToolSwitchEnabled() {
        if (getBooleanValueMethod == null || tweakToolSwitchEnum == null) {
            return false;
        }
        try {
            return (boolean) getBooleanValueMethod.invoke(tweakToolSwitchEnum);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 调用 Tweakeroo 的 InventoryUtils.trySwitchToEffectiveTool(BlockPos pos) 静态方法。
     * 只有在 Tweakeroo 存在且方法被成功加载时才执行。
     * @param pos 要挖掘的方块位置
     */
    public static void trySwitchToEffectiveTool(BlockPos pos) {
        if (trySwitchToEffectiveToolMethod == null) {
            return;
        }
        try {
            trySwitchToEffectiveToolMethod.invoke(null, pos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 本地版本（从fabric.mod.json读取）
    public static final String LOCAL_VERSION = getVersionFromModJson();

    public static void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            String latestVersion = getLatestPrinterVersion(LOCAL_VERSION);
            if (latestVersion == null) return;
            Minecraft.getInstance().execute(() -> MessageUtils.addMessage(
                    MessageUtils.translatable("litematica-printer.update.available", LOCAL_VERSION, latestVersion)
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))));
        });
    }

    /**
     * 从 fabric.mod.json 读取版本号
     * @return 版本号字符串，如果读取失败则返回 "unknown"
     */
    private static String getVersionFromModJson() {
        try {
            Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("litematica-printer");
            if (modContainer.isPresent()) {
                Optional<Path> modJsonPath = modContainer.get().findPath("fabric.mod.json");
                if (modJsonPath.isPresent() && Files.exists(modJsonPath.get())) {
                    try (InputStream inputStream = Files.newInputStream(modJsonPath.get());
                         InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                        return jsonObject.get("version").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    /**
     * 获取GitHub上最新的版本号
     * @param currentVersion 当前版本；正式版只接收正式版更新，测试版可升级到正式版
     * @return 最新版本号，如果获取失败则返回null
     */
    private static String getLatestPrinterVersion(String currentVersion) {
        try {
            URI uri = URI.create("https://api.github.com/repos/Neamyoo-dev/litematica-printer/releases");
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    String response = scanner.useDelimiter("\\A").next();
                    com.google.gson.JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
                    String latestTag = null;
                    Version latest = null;
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject release = jsonArray.get(i).getAsJsonObject();
                        String tagName = release.get("tag_name").getAsString();
                        boolean prerelease = release.get("prerelease").getAsBoolean();
                        boolean draft = release.get("draft").getAsBoolean();
                        if (!VersionUtils.isUpdateCandidate(currentVersion, tagName, prerelease, draft)) continue;
                        Version candidate = VersionUtils.parse(tagName);
                        if (latest == null || candidate.compareTo(latest) > 0) {
                            latestTag = tagName;
                            latest = candidate;
                        }
                    }
                    return latestTag;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
