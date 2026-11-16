package com.redabysslucia.dragonrise_reforge.network.message;

import com.redabysslucia.dragonrise_reforge.client.ClientMissileLaunchGate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server confirmation keeps local trigger prediction locked until the projectile actually spawns. */
public record MissileLaunchStateMessage(UUID vehicle, long token, boolean pending) {
    public static void encode(MissileLaunchStateMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.vehicle);
        buffer.writeVarLong(message.token);
        buffer.writeBoolean(message.pending);
    }

    public static MissileLaunchStateMessage decode(FriendlyByteBuf buffer) {
        return new MissileLaunchStateMessage(buffer.readUUID(), buffer.readVarLong(), buffer.readBoolean());
    }

    public static void handle(MissileLaunchStateMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) ClientMissileLaunchGate.receive(message);
        });
        context.get().setPacketHandled(true);
    }
}
