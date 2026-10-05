package me.aleksilassila.litematica.printer.printer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class PlacementGeometry {
    private PlacementGeometry() {}

    public static boolean useAirPlacement(boolean enabled, boolean requiresSupport, boolean hasSupport) {
        return enabled && !requiresSupport && !hasSupport;
    }

    public static Vec3 hitVec(BlockPos target, Direction side, Vec3 modifier, Direction facing) {
        Vec3 sideOffset = new Vec3(side.getStepX(), side.getStepY(), side.getStepZ()).scale(0.5);
        float radians = (float) Math.toRadians((facing.toYRot() + 90) % 360);
        return Vec3.atCenterOf(target).add(sideOffset).add(modifier.yRot(radians).scale(0.5));
    }

    public static PlayerLook lookAt(Vec3 eye, Vec3 target) {
        Vec3 delta = target.subtract(eye);
        return new PlayerLook(
                (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90),
                (float) -Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))));
    }
}
