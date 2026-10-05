package me.aleksilassila.litematica.printer.printer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlacementGeometryTest {
    @Test
    void realSupportTakesPriorityOverAirPlacement() {
        assertFalse(PlacementGeometry.useAirPlacement(true, false, true));
        assertTrue(PlacementGeometry.useAirPlacement(true, false, false));
        assertFalse(PlacementGeometry.useAirPlacement(true, true, false));
        assertFalse(PlacementGeometry.useAirPlacement(false, false, false));
    }

    @Test
    void rotatesHorizontalHitOffsetsByQuarterTurns() {
        Vec3 hit = PlacementGeometry.hitVec(BlockPos.ZERO, Direction.UP, new Vec3(0.5, 0, 0), Direction.SOUTH);
        assertEquals(0.5, hit.x, 0.00001);
        assertEquals(1.0, hit.y, 0.00001);
        assertEquals(0.25, hit.z, 0.00001);

        hit = PlacementGeometry.hitVec(BlockPos.ZERO, Direction.UP, new Vec3(0.5, 0, 0), Direction.NORTH);
        assertEquals(0.5, hit.x, 0.00001);
        assertEquals(0.75, hit.z, 0.00001);
    }

    @Test
    void preservesUpperHalfHitHeightInEveryHorizontalFacing() {
        BlockPos target = new BlockPos(-7, 64, 12);
        for (Direction facing : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            Vec3 hit = PlacementGeometry.hitVec(target, Direction.NORTH, new Vec3(0, 0.75, 0), facing);
            assertEquals(-6.5, hit.x, 0.00001);
            assertEquals(64.875, hit.y, 0.00001);
            assertEquals(12.0, hit.z, 0.00001);
        }
    }

    @Test
    void bucketRayLooksTowardsTargetInsteadOfThePlayersOriginalView() {
        PlayerLook north = PlacementGeometry.lookAt(Vec3.ZERO, new Vec3(0, 0, -3));
        assertEquals(-180, north.yaw(), 0.00001);
        assertEquals(0, north.pitch(), 0.00001);
        PlayerLook downSouth = PlacementGeometry.lookAt(Vec3.ZERO, new Vec3(0, -3, 3));
        assertEquals(0, downSouth.yaw(), 0.00001);
        assertEquals(45, downSouth.pitch(), 0.00001);
    }
}
