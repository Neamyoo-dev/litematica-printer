package me.aleksilassila.litematica.printer.handler.handlers;

import lombok.Getter;
import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.FillBlockModeType;
import me.aleksilassila.litematica.printer.enums.HighlightType;
import me.aleksilassila.litematica.printer.handler.Module;
import me.aleksilassila.litematica.printer.mixin.extension.BlockBreakResult;
import me.aleksilassila.litematica.printer.printer.*;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Fill extends Module {
    public final static String NAME = "fill";

    private List<String> fillCacheBlocklist = new ArrayList<>();
    @Getter
    private Item[] fillModeItemList = new Item[0];
    private FillBlockModeType lastFillMode;
    private BlockPos pendingBreakPos;
    private ClientLevel pendingBreakLevel;
    private Item pendingFillItem;

    // 可替换列表缓存，避免每位置都做拼音转换
    private List<String> replaceableCache = new ArrayList<>();
    private final Map<Block, Boolean> replaceableMatchCache = new HashMap<>();

    public Fill() {
        super(NAME, Configs.Fill.ENABLED, Configs.Fill.FILL_SELECTION_TYPE, true);
    }

    @Override
    protected int getTickInterval() {
        return Configs.Placement.PLACE_INTERVAL.getIntegerValue();
    }

    @Override
    protected int getMaxExecutions() {
        return Configs.Placement.PLACE_BLOCKS_PER_TICK.getIntegerValue();
    }

    @Override
    protected void preprocess() {
        FillBlockModeType fillMode = (FillBlockModeType) Configs.Fill.FILL_BLOCK_MODE.getOptionListValue();
        boolean modeChanged = fillMode != lastFillMode;
        lastFillMode = fillMode;
        // Mining may select a tool. Keep the original handheld fill material until this target is filled.
        if (pendingFillItem != null) {
            ItemStack pendingStack = pendingFillItem.getDefaultInstance();
            if (fillMode == FillBlockModeType.HANDHELD && Configs.Fill.FILL_DESTROY_BLOCKS.getBooleanValue()
                    && level == pendingBreakLevel && PlayerUtils.canInteracted(pendingBreakPos)
                    && LitematicaUtils.inSelection(pendingBreakPos)
                    && pendingFillItem instanceof BlockItem item
                    && !level.getBlockState(pendingBreakPos).is(item.getBlock())
                    && isAllowedHandheldItem(pendingStack)) {
                fillModeItemList = new Item[]{pendingFillItem};
                return;
            }
            clearPendingMaterial();
        }
        switch (fillMode) {
            case BLOCKLIST:
                // 每次去MC注册表中获取会造成大量卡顿, 所以仅在玩家修改了填充列表, 再去读取以便注册表
                List<String> strings = Configs.Fill.FILL_BLOCK_LIST.getStrings();
                if (modeChanged || !strings.equals(fillCacheBlocklist)) {
                    fillCacheBlocklist = new ArrayList<>(strings);
                    if (strings.isEmpty()) {
                        fillModeItemList = new Item[0];
                        return;
                    }
                    List<Item> items = new ArrayList<>();
                    for (String itemName : fillCacheBlocklist) {
                        items.addAll(BuiltInRegistries.ITEM
                                .stream()
                                .filter(item -> PinYinSearchUtils.matchName(itemName, new ItemStack(item)))
                                .toList()
                        );
                    }
                    fillModeItemList = items.toArray(new Item[0]);
                }
                break;
            case HANDHELD:  // 手持物品
                ItemStack heldStack = player.getMainHandItem();
                fillModeItemList = isAllowedHandheldItem(heldStack)
                        ? new Item[]{heldStack.getItem()} : new Item[0];
                break;
        }
    }

    private boolean isAllowedHandheldItem(ItemStack stack) {
        return !stack.isEmpty() && Configs.Fill.FILL_HANDHELD_BLACKLIST.getStrings().stream()
                .noneMatch(s -> PinYinSearchUtils.matchName(s, stack));
    }

    private void clearPendingMaterial() {
        pendingBreakPos = null;
        pendingBreakLevel = null;
        pendingFillItem = null;
    }

    @Override
    public void resetScanState() {
        super.resetScanState();
        clearPendingMaterial();
    }

    /**
     * 缓存版可替换判断：同种方块只做一次拼音匹配
     */
    private boolean isReplaceable(BlockState state) {
        List<String> current = Configs.Print.REPLACEABLE_LIST.getStrings();
        if (!current.equals(replaceableCache)) {
            replaceableCache = new ArrayList<>(current);
            replaceableMatchCache.clear();
        }
        if (current.isEmpty()) return false;
        return replaceableMatchCache.computeIfAbsent(state.getBlock(),
                block -> current.stream().anyMatch(s -> PinYinSearchUtils.matchName(s, state)));
    }

    @Override
    protected boolean canIterate() {
        return Configs.Fill.FILL_DESTROY_BLOCKS.getBooleanValue()
                ? Arrays.stream(fillModeItemList).anyMatch(item -> item instanceof BlockItem)
                : fillModeItemList.length > 0;
    }

    @Override
    public boolean canProcessPos(BlockPos pos) {
        if (!PlayerUtils.canInteracted(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return switch (getOperation(state)) {
            case KEEP -> false;
            case PLACE -> true;
            case DESTROY -> BreakUtils.canBreakBlock(pos) && BreakUtils.breakRestriction(state)
                    && level.getWorldBorder().isWithinBounds(pos);
        };
    }

    @Override
    public boolean isCorrectBlock(BlockPos pos) {
        return getOperation(level.getBlockState(pos)) == FillReplacementPolicy.Operation.KEEP;
    }

    private FillReplacementPolicy.Operation getOperation(BlockState state) {
        boolean matchesFillBlock = false;
        for (Item item : fillModeItemList) {
            if (item instanceof BlockItem blockItem && state.is(blockItem.getBlock())) {
                matchesFillBlock = true;
                break;
            }
        }
        boolean emptyOrLiquid = state.isAir() || state.getBlock() instanceof LiquidBlock;
        return FillReplacementPolicy.operation(Configs.Fill.FILL_DESTROY_BLOCKS.getBooleanValue(),
                matchesFillBlock, emptyOrLiquid || state.canBeReplaced(), emptyOrLiquid || isReplaceable(state));
    }

    @Override
    protected void executeIteration(BlockPos blockPos, AtomicReference<Boolean> skipIteration) {
        BlockState currentState = level.getBlockState(blockPos);
        FillReplacementPolicy.Operation operation = getOperation(currentState);
        if (operation == FillReplacementPolicy.Operation.KEEP) return;
        Item[] fillItems = Configs.Fill.FILL_DESTROY_BLOCKS.getBooleanValue()
                ? Arrays.stream(fillModeItemList).filter(item -> item instanceof BlockItem).toArray(Item[]::new)
                : fillModeItemList;
        // Do not destroy an obstacle unless a usable replacement is available.
        if (fillItems.length == 0) return;
        if (!InventoryUtils.switchToItems(player, fillItems)) {
            MissingMaterialTracker.getInstance().recordMissing(fillItems[0],
                    fillItems[0].getName(fillItems[0].getDefaultInstance()));
            return;
        }
        if (Configs.Placement.FALLING_CHECK.getBooleanValue() &&
            player.getMainHandItem().getItem() instanceof BlockItem item &&
            item.getBlock() instanceof FallingBlock block &&
            FallingBlock.isFree(level.getBlockState(blockPos.below()))
        ) {
            MessageUtils.setOverlayMessage(I18n.BLOCK_NO_SUPPORT.getName(block.getName().getString()));
            return;
        }

        if (operation == FillReplacementPolicy.Operation.DESTROY) {
            if (!BreakUtils.canBreakBlock(blockPos) || !BreakUtils.breakRestriction(currentState)
                    || !level.getWorldBorder().isWithinBounds(blockPos)) return;
            Item fillItem = player.getMainHandItem().getItem();
            // The creative path sends a real destroy packet with prediction, bypassing the mining delay.
            BlockBreakResult result = BreakUtils.INSTANCE.continueDestroyBlock(blockPos);
            if (result == BlockBreakResult.FAILED) {
                setCooldown(blockPos, Math.max(1, ConfigUtils.getBreakCooldown()));
                return;
            }
            addHighlight(blockPos, HighlightType.BREAK);
            if (result == BlockBreakResult.IN_PROGRESS || result == BlockBreakResult.COMPLETED_WAIT
                    || getOperation(level.getBlockState(blockPos)) == FillReplacementPolicy.Operation.DESTROY) {
                if (Configs.Fill.FILL_BLOCK_MODE.getOptionListValue() == FillBlockModeType.HANDHELD) {
                    pendingBreakPos = blockPos.immutable();
                    pendingBreakLevel = level;
                    pendingFillItem = fillItem;
                }
                enterWaiting(blockPos);
                skipIteration.set(true);
                if (result != BlockBreakResult.IN_PROGRESS) {
                    setCooldown(blockPos, Math.max(1, ConfigUtils.getBreakCooldown()));
                }
                return;
            }
            // Survival tool switching can also occur when mining finishes in one tick.
            if (!InventoryUtils.switchToItems(player, fillItems)) return;
        }

        Action action;
        if (ConfigUtils.getFillModeFacing() != null) {
            action = new Action()
                    .setLookDirection(ConfigUtils.getFillModeFacing().getOpposite())
                    .queueAction(blockPos, ConfigUtils.getFillModeFacing(), false, player);
        } else {
            action = new Action()
                    .queueAction(blockPos, getPlayerPlacementDirection(), false, player);
        }
        addHighlight(blockPos, HighlightType.PLACE);
        ActionManager.INSTANCE.setLook(action.getPlayerLook());
        ActionManager.INSTANCE.setNeedWaitModifyLookFromAction(action.getNeedWaitModifyLook());
        if (ActionManager.INSTANCE.sendQueue(player).needWaitModifyLook) {
            skipIteration.set(true);
        } else {
            this.setCooldown(blockPos, ConfigUtils.getPlaceCooldown());
        }
    }

}
