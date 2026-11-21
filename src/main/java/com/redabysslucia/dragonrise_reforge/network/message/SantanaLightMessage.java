package com.redabysslucia.dragonrise_reforge.network.message;

import com.redabysslucia.dragonrise_reforge.entities.SantanaEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 桑塔纳「点灯」开关（客户端 → 服务器）。
 *
 * <p>V 键在客户端触发，但动画与循环音是靠实体同步数据驱动的：
 * 状态位存在 {@link SantanaEntity#dr$isLightOn()}，由服务端改、再同步给所有客户端，
 * 这样旁观者也能看到动画、听到循环音。服务端只接受"自己正坐在该车里"的玩家请求。
 */
public class SantanaLightMessage {

    private final int entityId;

    public SantanaLightMessage(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(SantanaLightMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
    }

    public static SantanaLightMessage decode(FriendlyByteBuf buf) {
        return new SantanaLightMessage(buf.readVarInt());
    }

    public static void handle(SantanaLightMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
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
        ctx.get().setPacketHandled(true);
    }
}
