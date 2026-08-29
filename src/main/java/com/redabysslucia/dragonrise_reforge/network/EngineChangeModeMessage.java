package com.redabysslucia.dragonrise_reforge.network;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.VariableEngineVehicle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EngineChangeModeMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EngineChangeModeMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "engine_change_mode"));

    public static final StreamCodec<FriendlyByteBuf, EngineChangeModeMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {},
            buf -> new EngineChangeModeMessage()
    );

    public EngineChangeModeMessage() {}

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static EngineChangeModeMessage decode(FriendlyByteBuf buffer) {
        return new EngineChangeModeMessage();
    }

    public static void handler(EngineChangeModeMessage message, java.util.function.Supplier<net.neoforged.neoforge.network.NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            var vehicle = player.getVehicle();
            if (vehicle instanceof VariableEngineVehicle vtol) {
                vtol.toggleChangeMode();
            }
        });
    }
}
