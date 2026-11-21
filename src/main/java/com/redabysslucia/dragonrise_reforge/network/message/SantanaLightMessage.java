package com.redabysslucia.dragonrise_reforge.network.message;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.SantanaEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 桑塔纳「点灯」开关（客户端 → 服务器）。
 *
 * <p>V 键在客户端触发，但动画与循环音靠实体同步数据驱动：状态位存在
 * {@link SantanaEntity#dr$isLightOn()}，由服务端改、再同步给所有客户端，
 * 这样旁观者也能看到动画、听到循环音。服务端只接受"自己正坐在该车里"的玩家请求。
 *
 * <p>1.21.1 走 NeoForge 的 payload 体系（{@link CustomPacketPayload} + {@link StreamCodec}）。
 */
public class SantanaLightMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SantanaLightMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "santana_light"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SantanaLightMessage> STREAM_CODEC =
            StreamCodec.of(SantanaLightMessage::encode, SantanaLightMessage::decode);

    private final int entityId;

    public SantanaLightMessage(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(RegistryFriendlyByteBuf buf, SantanaLightMessage msg) {
        buf.writeVarInt(msg.entityId);
    }

    public static SantanaLightMessage decode(RegistryFriendlyByteBuf buf) {
        return new SantanaLightMessage(buf.readVarInt());
    }

    public static void handle(SantanaLightMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            Entity vehicle = player.level().getEntity(msg.entityId);
            if (!(vehicle instanceof SantanaEntity santana)) {
                return;
            }
            // 只有驾驶 / 乘坐该车的人才能开关
            if (player.getVehicle() != santana) {
                return;
            }
            santana.dr$toggleLight();
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
