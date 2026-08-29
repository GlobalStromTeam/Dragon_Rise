package com.redabysslucia.dragonrise_reforge.network.message;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase;
import com.redabysslucia.dragonrise_reforge.firecontrol.TrajectoryMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SetFireControlMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetFireControlMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "set_fire_control"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetFireControlMessage> STREAM_CODEC =
            StreamCodec.of(SetFireControlMessage::encode, SetFireControlMessage::decode);

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

    public static void encode(RegistryFriendlyByteBuf buf, SetFireControlMessage msg) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.clear);
        if (!msg.clear) {
            buf.writeBlockPos(msg.target);
            buf.writeVarInt(msg.radius);
            buf.writeEnum(msg.trajectoryMode);
            buf.writeBoolean(msg.takeover);
        }
    }

    public static SetFireControlMessage decode(RegistryFriendlyByteBuf buf) {
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

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetFireControlMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
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
    }
}
