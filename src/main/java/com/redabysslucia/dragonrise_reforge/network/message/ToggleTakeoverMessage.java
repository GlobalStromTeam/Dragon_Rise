package com.redabysslucia.dragonrise_reforge.network.message;

import com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleTakeoverMessage {

    private final int entityId;
    private final boolean takeover;

    public ToggleTakeoverMessage(int entityId, boolean takeover) {
        this.entityId = entityId;
        this.takeover = takeover;
    }

    public static void encode(ToggleTakeoverMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.takeover);
    }

    public static ToggleTakeoverMessage decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        boolean takeover = buf.readBoolean();
        return new ToggleTakeoverMessage(entityId, takeover);
    }

    public static void handle(ToggleTakeoverMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Entity entity = player.level().getEntity(msg.entityId);
            if (!(entity instanceof IndirectFireVehicleBase vehicle)) return;
            // 验证玩家是炮塔控制器
            if (vehicle.getSeatIndex(player) != vehicle.getTurretControllerIndex()) return;
            vehicle.setFireControlTakeover(msg.takeover, player);
        });
        ctx.get().setPacketHandled(true);
    }
}
