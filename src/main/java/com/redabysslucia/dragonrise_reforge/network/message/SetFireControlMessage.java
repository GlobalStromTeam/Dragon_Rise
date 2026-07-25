package com.redabysslucia.dragonrise_reforge.network.message;

import com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase;
import com.redabysslucia.dragonrise_reforge.firecontrol.TrajectoryMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetFireControlMessage {

    private final int entityId;
    private final boolean clear;
    private final BlockPos target;
    private final int radius;
    private final TrajectoryMode trajectoryMode;
    private final boolean takeover;

    public SetFireControlMessage(int entityId, boolean clear, BlockPos target, int radius, TrajectoryMode trajectoryMode, boolean takeover) {
        this.entityId = entityId;
        this.clear = clear;
        this.target = target;
        this.radius = radius;
        this.trajectoryMode = trajectoryMode;
        this.takeover = takeover;
    }

    public static SetFireControlMessage apply(int entityId, BlockPos target, int radius, TrajectoryMode trajectoryMode, boolean takeover) {
        return new SetFireControlMessage(entityId, false, target, radius, trajectoryMode, takeover);
    }

    public static SetFireControlMessage clear(int entityId) {
        return new SetFireControlMessage(entityId, true, BlockPos.ZERO, 0, TrajectoryMode.LOW, false);
    }

    public static void encode(SetFireControlMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.clear);
        if (!msg.clear) {
            buf.writeBlockPos(msg.target);
            buf.writeVarInt(msg.radius);
            buf.writeEnum(msg.trajectoryMode);
            buf.writeBoolean(msg.takeover);
        }
    }

    public static SetFireControlMessage decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        boolean clear = buf.readBoolean();
        if (clear) {
            return clear(entityId);
        }
        BlockPos target = buf.readBlockPos();
        int radius = buf.readVarInt();
        TrajectoryMode trajectoryMode = buf.readEnum(TrajectoryMode.class);
        boolean takeover = buf.readBoolean();
        return new SetFireControlMessage(entityId, false, target, radius, trajectoryMode, takeover);
    }

    public static void handle(SetFireControlMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Entity entity = player.level().getEntity(msg.entityId);
            if (!(entity instanceof IndirectFireVehicleBase vehicle)) return;
            // 验证玩家是炮塔控制器
            if (vehicle.getSeatIndex(player) != vehicle.getTurretControllerIndex()) return;
            if (msg.clear) {
                vehicle.clearFireControl(player);
            } else {
                vehicle.applyFireControl(msg.target, msg.radius, msg.trajectoryMode, player);
                if (msg.takeover) {
                    vehicle.setFireControlTakeover(true, player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
