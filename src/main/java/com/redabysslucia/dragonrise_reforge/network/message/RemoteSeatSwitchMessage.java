package com.redabysslucia.dragonrise_reforge.network.message;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.atsuishio.superbwarfare.tools.NBTTool;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 遥控切中位（客户端 → 服务器）：Shift + 数字键。
 * <p>
 * 与 SBW 原生 {@code ChangeVehicleSeatMessage} 的区别：官方那条只服务真乘员
 * （{@code vehicle.changeSeat(player, index)} 走乘员列表），遥控者不在载具上，
 * 所以中位是记录在监控平板 NBT 上的，由本模组自己维护。
 * 服务端处理完再把权威结果用 {@link RemoteSeatSyncMessage} 发回来，
 * 客户端以服务端结果为准（防止两台遥控者抢同一席位时本地状态错乱）。
 *
 * <p>1.21.1 迁移：Forge 的 SimpleChannel / NetworkEvent.Context 已移除，
 * 改为 NeoForge 的 {@link CustomPacketPayload} + {@link StreamCodec} + {@link IPayloadContext}。
 */
public class RemoteSeatSwitchMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RemoteSeatSwitchMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "remote_seat_switch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteSeatSwitchMessage> STREAM_CODEC =
            StreamCodec.of(RemoteSeatSwitchMessage::encode, RemoteSeatSwitchMessage::decode);

    private final int index;

    public RemoteSeatSwitchMessage(int index) {
        this.index = index;
    }

    public static void encode(RegistryFriendlyByteBuf buf, RemoteSeatSwitchMessage msg) {
        buf.writeVarInt(msg.index);
    }

    public static RemoteSeatSwitchMessage decode(RegistryFriendlyByteBuf buf) {
        return new RemoteSeatSwitchMessage(buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoteSeatSwitchMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.MONITOR.get())) {
                return;
            }
            var tag = NBTTool.getTag(stack);
            if (!tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
                return;
            }

            var drone = EntityFindUtil.findDrone(player.level(), tag.getString(MonitorItem.LINKED_DRONE));
            if (!(drone instanceof ShuangweixieEntity aircraft)) {
                return;
            }

            int authoritative = aircraft.dr$switchRemoteSeat(player, msg.index);
            PacketDistributor.sendToPlayer(player, new RemoteSeatSyncMessage(authoritative));
        });
    }
}
