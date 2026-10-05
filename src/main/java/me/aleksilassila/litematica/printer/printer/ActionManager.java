package me.aleksilassila.litematica.printer.printer;

import lombok.Setter;
import me.aleksilassila.litematica.printer.Reference;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.mixin.extension.MultiPlayerGameModeExtension;
import me.aleksilassila.litematica.printer.utils.BlockUtils;
import me.aleksilassila.litematica.printer.utils.PacketUtils;
import me.aleksilassila.litematica.printer.utils.InventoryUtils;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

public class ActionManager {
    public static final ActionManager INSTANCE = new ActionManager();

    public BlockPos target;
    public Direction side;
    public Vec3 hitModifier;
    public boolean useShift = false;
    public boolean useProtocol = false;
    public boolean useItem = false;
    @Setter
    @Nullable
    public PlayerLook look;
    public boolean needWaitModifyLook = false;
    private boolean actionRequiresWaitModifyLook = false;

    private ActionManager() {
    }

    public void queueClick(@NotNull BlockPos target, @NotNull Direction side, @NotNull Vec3 hitModifier, boolean useShift) {
        if (Configs.Placement.PLACE_INTERVAL.getIntegerValue() != 0) {
            if (this.target != null) {
                System.out.println("Was not ready yet.");
                return;
            }
        }
        this.target = target;
        this.side = side;
        this.hitModifier = hitModifier;
        this.useShift = useShift;
    }

    public ActionManager sendQueue(LocalPlayer player) {
        if (player == null || Reference.MINECRAFT.level == null || Reference.MINECRAFT.gameMode == null
                || target == null || side == null || hitModifier == null) {
            clearQueue();
            return this;
        }
        if (look != null) {
            PacketUtils.sendLookPacket(player, look);
        }

        if (!useProtocol && !needWaitModifyLook && actionRequiresWaitModifyLook) {
            if (look != null) {
                Direction lookDirection = BlockUtils.orderedByNearest(look.yaw(), look.pitch())[0];
                if (lookDirection.getAxis().isHorizontal()) {
                    needWaitModifyLook = true;
                    return this;
                }
            }
        }

        if (needWaitModifyLook) {
            needWaitModifyLook = false;
        }

        Direction direction;
        if (look == null) {
            direction = side;
        } else {
            direction = BlockUtils.getHorizontalDirection(look.yaw());
        }
        Vec3 hitVec;
        if (!useProtocol) {
            hitVec = PlacementGeometry.hitVec(target, side, hitModifier, direction);
        } else {
            hitVec = hitModifier;
        }
        boolean wasSneak = player.isShiftKeyDown();
        if (useShift && !wasSneak) {
            setShift(player, true);
        } else if (!useShift && wasSneak) {
            setShift(player, false);
        }
        MultiPlayerGameModeExtension gameModeExtension = (MultiPlayerGameModeExtension) Reference.MINECRAFT.gameMode;
        if (gameModeExtension != null) {
            if (useItem && look != null) {
                PacketUtils.sendPacket(new ServerboundSetCarriedItemPacket(InventoryUtils.getSelectedSlot(player.getInventory())));
                PlayerLook itemLook = look;
                PacketUtils.sendPacket(sequence -> new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND, sequence, itemLook.yaw(), itemLook.pitch()));
            } else {
                boolean localPrediction = !Configs.Placement.PRINT_USE_PACKET.getBooleanValue();
                BlockHitResult blockHitResult = new BlockHitResult(hitVec, side, target, false);
                gameModeExtension.litematica_printer$useItemOn(localPrediction, InteractionHand.MAIN_HAND, blockHitResult);
            }
        }
        if (useShift && !wasSneak) {
            setShift(player, false);
        } else if (!useShift && wasSneak) {
            setShift(player, true);
        }
        clearQueue();
        return this;
    }

    public void setNeedWaitModifyLookFromAction(boolean needWaitModifyLook) {
        this.actionRequiresWaitModifyLook = needWaitModifyLook;
    }

    public void setShift(LocalPlayer player, boolean shift) {
        Input input = new Input(player.input.keyPresses.forward(), player.input.keyPresses.backward(), player.input.keyPresses.left(), player.input.keyPresses.right(), player.input.keyPresses.jump(), shift, player.input.keyPresses.sprint());
        ServerboundPlayerInputPacket packet = new ServerboundPlayerInputPacket(input);
        player.setShiftKeyDown(shift);
        PacketUtils.sendPacket(packet);
    }

    public void clearQueue() {
        this.target = null;
        this.side = null;
        this.hitModifier = null;
        this.useShift = false;
        this.useProtocol = false;
        this.useItem = false;
        this.needWaitModifyLook = false;
        this.actionRequiresWaitModifyLook = false;
        this.look = null;
    }
}
