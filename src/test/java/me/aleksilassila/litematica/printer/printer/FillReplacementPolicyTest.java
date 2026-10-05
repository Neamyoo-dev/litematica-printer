package me.aleksilassila.litematica.printer.printer;

import org.junit.jupiter.api.Test;

import static me.aleksilassila.litematica.printer.printer.FillReplacementPolicy.Operation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FillReplacementPolicyTest {
    @Test
    void disabledDestroyPreservesSolidObstacles() {
        assertEquals(KEEP, FillReplacementPolicy.operation(false, false, false, false));
        assertEquals(PLACE, FillReplacementPolicy.operation(false, false, true, true));
    }

    @Test
    void destroyThenFillStopsAfterTheTargetHasBeenFilled() {
        assertEquals(DESTROY, FillReplacementPolicy.operation(true, false, false, false));
        assertEquals(PLACE, FillReplacementPolicy.operation(true, false, true, true));
        assertEquals(KEEP, FillReplacementPolicy.operation(true, true, false, false));
    }

    @Test
    void configuredReplaceableSolidsStillNeedDestruction() {
        assertEquals(DESTROY, FillReplacementPolicy.operation(true, false, false, true));
        assertEquals(PLACE, FillReplacementPolicy.operation(false, false, false, true));
    }

    @Test
    void matchingFillBlocksAreNeverRepeatedlyDestroyedOrPlaced() {
        assertEquals(KEEP, FillReplacementPolicy.operation(true, true, false, true));
        assertEquals(KEEP, FillReplacementPolicy.operation(true, true, true, true));
    }
}
