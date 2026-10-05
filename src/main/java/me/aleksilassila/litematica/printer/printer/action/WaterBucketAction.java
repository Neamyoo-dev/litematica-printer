package me.aleksilassila.litematica.printer.printer.action;

import me.aleksilassila.litematica.printer.Reference;
import me.aleksilassila.litematica.printer.printer.ActionManager;
import me.aleksilassila.litematica.printer.printer.PlacementGeometry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Buckets use an item-use ray cast instead of a block-item placement packet. */
public class WaterBucketAction extends Action {
    private final boolean waterlogExistingBlock;
    private BlockHitResult hit;

    public WaterBucketAction(boolean waterlogExistingBlock) {
        this.waterlogExistingBlock = waterlogExistingBlock;
        setItem(Items.WATER_BUCKET);
        setRequiresSupport();
    }

    @Override
    public @Nullable Direction getValidSide(ClientLevel world, BlockPos pos) {
        LocalPlayer player = Reference.MINECRAFT.player;
        if (player == null) return null;
        Vec3 eye = player.getEyePosition();
        for (Direction side : getSides().keySet()) {
            BlockPos clickedPos = waterlogExistingBlock ? pos : pos.relative(side);
            Direction face = waterlogExistingBlock ? side : side.getOpposite();
            Vec3 aim = Vec3.atCenterOf(clickedPos).add(
                    face.getStepX() * 0.499, face.getStepY() * 0.499, face.getStepZ() * 0.499);
            Vec3 delta = aim.subtract(eye);
            BlockHitResult ray = world.clip(new ClipContext(eye,
                    eye.add(delta.normalize().scale(player.blockInteractionRange())),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            if (ray.getType() != HitResult.Type.BLOCK || !ray.getBlockPos().equals(clickedPos)) continue;
            if (!waterlogExistingBlock && !clickedPos.relative(ray.getDirection()).equals(pos)) continue;
            hit = ray;
            playerLook = PlacementGeometry.lookAt(eye, aim);
            return side;
        }
        return null;
    }

    @Override
    public Action queueAction(BlockPos pos, Direction side, boolean useShift, LocalPlayer player) {
        ActionManager.INSTANCE.queueClick(hit.getBlockPos(), hit.getDirection(), Vec3.ZERO, false);
        ActionManager.INSTANCE.useItem = true;
        return this;
    }
}
