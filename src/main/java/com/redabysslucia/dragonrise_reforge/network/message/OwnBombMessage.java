package com.redabysslucia.dragonrise_reforge.network.message;

import com.redabysslucia.dragonrise_reforge.events.ClientEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 服务端 → 客户端：告知发射者玩家"某颗制导炸弹是你的"（携带炸弹实体 UUID）。
 * 背景：客户端炸弹实体的 owner(entityData) 因 superb BVR 同步机制不会到达客户端，
 * 无法用 getOwner() 判断归属，改由服务端在炸弹生成时直接通知。
 */
public class OwnBombMessage {
    private final UUID bombUuid;

    public OwnBombMessage(UUID bombUuid) {
        this.bombUuid = bombUuid;
    }

    public static void encode(OwnBombMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.bombUuid);
    }

    public static OwnBombMessage decode(FriendlyByteBuf buffer) {
        return new OwnBombMessage(buffer.readUUID());
    }

    public static void handle(OwnBombMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientEvent.onOwnBombMessage(message.bombUuid));
        ctx.get().setPacketHandled(true);
    }
}
