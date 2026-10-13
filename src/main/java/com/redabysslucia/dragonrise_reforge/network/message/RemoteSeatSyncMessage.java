package com.redabysslucia.dragonrise_reforge.network.message;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.tools.NBTTool;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 遥控席位权威同步（服务器 → 客户端）：把玩家当前席位写回本地监控平板 NBT。
 * <p>
 * 客户端 {@code getSeatIndex} / 座位相机 / 战斗机 HUD 都直接读本地平板，所以需要这份同步；
 * 服务器拒绝切席位（目标席位已被别的遥控者占用）时，这里也会把本地改回原席位。
 *
 * <p>1.21.1 迁移：改用 NeoForge 的 payload 体系（与 {@link RemoteSeatSwitchMessage} 同）。
 * 写 NBT 必须走 {@link NBTTool#getTag} 读 + {@link NBTTool#saveTag} 写回 ——
 * {@code ItemStack#getOrCreateTag()} 在 1.21.1 已被移除，直接改 tag 不会生效。
 */
public class RemoteSeatSyncMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RemoteSeatSyncMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "remote_seat_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteSeatSyncMessage> STREAM_CODEC =
            StreamCodec.of(RemoteSeatSyncMessage::encode, RemoteSeatSyncMessage::decode);

    private final int index;

    public RemoteSeatSyncMessage(int index) {
        this.index = index;
    }

    public static void encode(RegistryFriendlyByteBuf buf, RemoteSeatSyncMessage msg) {
        buf.writeVarInt(msg.index);
    }

    public static RemoteSeatSyncMessage decode(RegistryFriendlyByteBuf buf) {
        return new RemoteSeatSyncMessage(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoteSeatSyncMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player == null) {
                return;
            }
            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.MONITOR.get())) {
                return;
            }
            var tag = NBTTool.getTag(stack);
            tag.putInt(ShuangweixieEntity.DR_REMOTE_SEAT, msg.index);
            NBTTool.saveTag(stack, tag);
        });
    }
}
