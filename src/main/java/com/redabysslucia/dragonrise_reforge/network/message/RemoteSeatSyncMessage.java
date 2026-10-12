package com.redabysslucia.dragonrise_reforge.network.message;

import com.atsuishio.superbwarfare.init.ModItems;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 遥控席位权威同步（服务器 → 客户端）：把玩家当前席位写回本地监控平板 NBT。
 * <p>
 * 客户端 {@code getSeatIndex} / 座位相机 / 战斗机 HUD 都直接读本地平板，所以需要这份同步；
 * 服务器拒绝切席位（目标席位已被别的遥控者占用）时，这里也会把本地改回原席位。
 */
public class RemoteSeatSyncMessage {

    private final int index;

    public RemoteSeatSyncMessage(int index) {
        this.index = index;
    }

    public static void encode(RemoteSeatSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.index);
    }

    public static RemoteSeatSyncMessage decode(FriendlyByteBuf buf) {
        return new RemoteSeatSyncMessage(buf.readVarInt());
    }

    public static void handle(RemoteSeatSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            var player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.MONITOR.get())) {
                return;
            }
            stack.getOrCreateTag().putInt(ShuangweixieEntity.DR_REMOTE_SEAT, msg.index);
        });
        ctx.get().setPacketHandled(true);
    }
}
