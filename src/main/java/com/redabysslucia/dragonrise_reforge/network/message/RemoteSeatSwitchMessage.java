package com.redabysslucia.dragonrise_reforge.network.message;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;

import java.util.function.Supplier;

/**
 * 遥控切席位（客户端 → 服务器）：Shift + 数字键。
 * <p>
 * 与 SBW 原生 {@code ChangeVehicleSeatMessage} 的区别：官方那条只服务"真乘客"
 * （{@code vehicle.changeSeat(player, index)} 走乘客列表），遥控者不在载具上，
 * 所以席位是记录在监控平板 NBT 上的，由本模组自己维护。
 * 服务端处理完再把权威结果用 {@link RemoteSeatSyncMessage} 发回来，
 * 客户端以服务端结果为准（防止两台遥控者抢同一席位时本地状态错乱）。
 */
public class RemoteSeatSwitchMessage {

    private final int index;

    public RemoteSeatSwitchMessage(int index) {
        this.index = index;
    }

    public static void encode(RemoteSeatSwitchMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.index);
    }

    public static RemoteSeatSwitchMessage decode(FriendlyByteBuf buf) {
        return new RemoteSeatSwitchMessage(buf.readVarInt());
    }

    public static void handle(RemoteSeatSwitchMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.MONITOR.get())) {
                return;
            }
            var tag = stack.getOrCreateTag();
            if (!tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
                return;
            }

            var drone = EntityFindUtil.findDrone(player.level(), tag.getString(MonitorItem.LINKED_DRONE));
            if (!(drone instanceof ShuangweixieEntity aircraft)) {
                return;
            }

            int authoritative = aircraft.dr$switchRemoteSeat(player, msg.index);
            ModNetwork.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player),
                    new RemoteSeatSyncMessage(authoritative));
        });
        ctx.get().setPacketHandled(true);
    }
}
