package me.aleksilassila.litematica.printer.utils;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EasyPlaceCompatibilityTest {
    private boolean initialHandling;

    @BeforeEach
    void saveHandlingState() {
        initialHandling = EasyPlaceUtils.isHandling();
        EasyPlaceUtils.setHandling(false);
    }

    @AfterEach
    void restoreHandlingState() {
        EasyPlaceUtils.setHandling(initialHandling);
    }

    @Test
    void printerTargetIsNotReplacedByTheManualEasyPlaceTarget() {
        BlockPos printerTarget = new BlockPos(-451, 112, 4999873);
        BlockPos crosshairTarget = printerTarget.above();
        // Litematica's interaction mixin redirects to the crosshair unless isHandling is true.
        BlockPos placedAt = EasyPlaceCompatibility.runPrinterPlacement(
                () -> EasyPlaceUtils.isHandling() ? printerTarget : crosshairTarget);
        assertEquals(printerTarget, placedAt);
        assertFalse(EasyPlaceUtils.isHandling());
    }

    @Test
    void nestedPlacementKeepsTheOuterEasyPlaceOperationActive() {
        EasyPlaceUtils.setHandling(true);
        EasyPlaceCompatibility.runPrinterPlacement(() -> {
            EasyPlaceCompatibility.runPrinterPlacement(() -> {
                assertTrue(EasyPlaceUtils.isHandling());
                return null;
            });
            assertTrue(EasyPlaceUtils.isHandling());
            return null;
        });
        assertTrue(EasyPlaceUtils.isHandling());
    }

    @Test
    void placementFailureDoesNotDisableSubsequentManualEasyPlace() {
        IllegalStateException failure = new IllegalStateException("placement failed");
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> EasyPlaceCompatibility.runPrinterPlacement(() -> {
                    assertTrue(EasyPlaceUtils.isHandling());
                    throw failure;
                })));
        assertFalse(EasyPlaceUtils.isHandling());
    }
}
