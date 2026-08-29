package com.redabysslucia.dragonrise_reforge.network.message;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.events.ClientEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * 服务端 → 客户端：告知发射者玩家"某颗制导炸弹是你的"（携带炸弹实体 UUID）。
 * 背景：客户端炸弹实体的 owner(entityData) 因 superb BVR 同步机制不会到达客户端，
 * 无法用 getOwner() 判断归属，改由服务端在炸弹生成时直接通知。
 */
public class OwnBombMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OwnBombMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "own_bomb"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OwnBombMessage> STREAM_CODEC =
            StreamCodec.of(OwnBombMessage::encode, OwnBombMessage::decode);

    private final UUID bombUuid;

    public OwnBombMessage(UUID bombUuid) {
        this.bombUuid = bombUuid;
    }

    public static void encode(RegistryFriendlyByteBuf buf, OwnBombMessage msg) {
        buf.writeUUID(msg.bombUuid);
    }

    public static OwnBombMessage decode(RegistryFriendlyByteBuf buf) {
        return new OwnBombMessage(buf.readUUID());
    }

    public UUID bombUuid() {
        return this.bombUuid;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OwnBombMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientEvent.onOwnBombMessage(msg.bombUuid));
    }
}
