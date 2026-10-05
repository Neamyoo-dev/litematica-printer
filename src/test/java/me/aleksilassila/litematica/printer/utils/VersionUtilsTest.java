package me.aleksilassila.litematica.printer.utils;

import net.fabricmc.loader.api.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class VersionUtilsTest {
    @ParameterizedTest
    @CsvSource({
            "1.3-beta.5-mc26.2-1799-217f9515-development, 1.4.0-beta.1",
            "1.4.0-beta.2, 1.4.0-beta.10",
            "1.4.0-alpha.1, 1.4.0-beta.1",
            "1.4.0-beta.1, 1.4.0-rc.1",
            "1.4.0-rc.1, 1.4.0",
            "1.4.0, 1.4.1",
            "1.4.0, 1.10.0"
    })
    void comparesReleaseIdentifiersNumerically(String older, String newer) {
        Version oldVersion = VersionUtils.parse(older);
        Version newVersion = VersionUtils.parse(newer);
        assertNotNull(oldVersion);
        assertNotNull(newVersion);
        assertTrue(newVersion.compareTo(oldVersion) > 0);
        assertTrue(oldVersion.compareTo(newVersion) < 0);
    }

    @Test
    void ignoresBuildMetadataAndAcceptsReleaseTags() {
        Version beta = VersionUtils.parse("v1.4.0-beta.1");
        Version stable = VersionUtils.parse("1.4.0+development.43.gdef5678");
        assertEquals(0, beta.compareTo(
                VersionUtils.parse("1.4.0-beta.1+snapshot.42.gabc1234")));
        assertEquals(0, stable.compareTo(
                VersionUtils.parse("1.4.0+snapshot.42.gabc1234")));
    }

    @Test
    void prereleaseUsersCanUpgradeToTheStableRelease() {
        assertTrue(VersionUtils.isUpdateCandidate("1.4.0-beta.1", "v1.4.0", false, false));
        assertTrue(VersionUtils.isUpdateCandidate("1.4.0-beta.1", "v1.4.0-beta.2", true, false));
    }

    @Test
    void stableUsersDoNotReceivePrereleasesEvenIfGitHubMarksThemStable() {
        assertFalse(VersionUtils.isUpdateCandidate("1.4.0", "v1.5.0-beta.1", true, false));
        assertFalse(VersionUtils.isUpdateCandidate("1.4.0", "v1.5.0-rc.1", false, false));
        assertFalse(VersionUtils.isUpdateCandidate("1.4.0", "v1.5.0", true, false));
        assertTrue(VersionUtils.isUpdateCandidate("1.4.0", "v1.4.1", false, false));
    }

    @Test
    void rejectsDraftsOlderVersionsAndBuildOnlyChanges() {
        assertFalse(VersionUtils.isUpdateCandidate("1.4.0-beta.1", "v1.4.0", false, true));
        assertFalse(VersionUtils.isUpdateCandidate("1.4.0-beta.2", "v1.4.0-beta.1", true, false));
        assertFalse(VersionUtils.isUpdateCandidate("1.4.0-beta.1+development.1.gabc", "v1.4.0-beta.1", true, false));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"unknown", "not-a-version", "1.4.x", "1.4.0-", "1.4.0+",
            "1.4.0-beta.01", "01.4.0", "1.4.0+g_123", "1.4.0.1"})
    void invalidVersionsAreNotUpdateCandidates(String invalid) {
        assertNull(VersionUtils.parse(invalid));
        assertFalse(VersionUtils.isUpdateCandidate("1.4.0-beta.1", invalid, false, false));
    }
}
