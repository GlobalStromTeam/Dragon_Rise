package com.redabysslucia.dragonrise_reforge.network.message;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ToggleTakeoverMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleTakeoverMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "toggle_takeover"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleTakeoverMessage> STREAM_CODEC =
            StreamCodec.of(ToggleTakeoverMessage::encode, ToggleTakeoverMessage::decode);

    private final int entityId;
    private final boolean takeover;

    public ToggleTakeoverMessage(int entityId, boolean takeover) {
        this.entityId = entityId;
        this.takeover = takeover;
    }

    public static void encode(RegistryFriendlyByteBuf buf, ToggleTakeoverMessage msg) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.takeover);
    }

    public static ToggleTakeoverMessage decode(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        boolean takeover = buf.readBoolean();
        return new ToggleTakeoverMessage(entityId, takeover);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleTakeoverMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player == null) return;
            Entity entity = player.level().getEntity(msg.entityId);
            if (!(entity instanceof IndirectFireVehicleBase vehicle)) return;
            // 验证玩家是炮塔控制器
            if (vehicle.getSeatIndex(player) != vehicle.getTurretControllerIndex()) return;
            vehicle.setFireControlTakeover(msg.takeover, player);
        });
    }
}
