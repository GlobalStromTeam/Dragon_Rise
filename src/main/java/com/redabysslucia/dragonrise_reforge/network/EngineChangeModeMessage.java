package com.redabysslucia.dragonrise_reforge.network;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.VariableEngineVehicle;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端 → 服务器：切换变循环/矢量发动机模式（无载荷数据）。
 */
public class EngineChangeModeMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EngineChangeModeMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "engine_change_mode"));

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, EngineChangeModeMessage> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> {
            }, buf -> new EngineChangeModeMessage());

    public EngineChangeModeMessage() {
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EngineChangeModeMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) return;
            var vehicle = player.getVehicle();
            if (vehicle instanceof VariableEngineVehicle vtol) {
                vtol.toggleChangeMode();
            }
        });
    }
}
