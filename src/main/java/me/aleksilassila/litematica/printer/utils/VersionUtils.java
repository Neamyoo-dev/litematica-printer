package me.aleksilassila.litematica.printer.utils;

import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public final class VersionUtils {
    private static final String NUMBER = "(?:0|[1-9][0-9]*)";
    private static final String IDENTIFIER = "(?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)";
    private static final Pattern SEMVER = Pattern.compile("^" + NUMBER + "\\." + NUMBER + "\\." + NUMBER
            + "(?:-" + IDENTIFIER + "(?:\\." + IDENTIFIER + ")*)?"
            + "(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$");

    private VersionUtils() {}

    public static @Nullable SemanticVersion parse(@Nullable String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.startsWith("v")) normalized = normalized.substring(1);
        // Previous jars included Minecraft and Git identifiers in the version itself.
        normalized = normalized.replaceFirst("-mc\\d+(?:\\.\\d+)+.*$", "");
        normalized = normalized.replaceFirst("^([0-9]+\\.[0-9]+)(?=[-+]|$)", "$1.0");
        normalized = normalized.replaceFirst("^([0-9]+)(?=[-+]|$)", "$1.0.0");
        if (!SEMVER.matcher(normalized).matches()) return null;
        try {
            return SemanticVersion.parse(normalized);
        } catch (VersionParsingException e) {
            return null;
        }
    }

    public static boolean isUpdateCandidate(String current, String candidate, boolean prerelease, boolean draft) {
        if (draft) return false;
        SemanticVersion local = parse(current);
        SemanticVersion remote = parse(candidate);
        if (local == null || remote == null) return false;
        boolean stableUser = local.getPrereleaseKey().isEmpty();
        if (stableUser && (prerelease || remote.getPrereleaseKey().isPresent())) return false;
        return ((Version) remote).compareTo(local) > 0;
    }
}
