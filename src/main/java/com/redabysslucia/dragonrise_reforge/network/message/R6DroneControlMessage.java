package com.redabysslucia.dragonrise_reforge.network.message;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 侦察无人车键盘输入消息（客户端 → 服务器）：
 * 携带 WASD/空格/冲刺 的按键位标志，服务器写入对应无人车实体。
 * 鼠标/视角无需传输 —— 服务器直接读取控制者玩家的旋转。
 */
public class R6DroneControlMessage {

    private final short keys;

    public R6DroneControlMessage(short keys) {
        this.keys = keys;
    }

    public static void encode(R6DroneControlMessage msg, FriendlyByteBuf buf) {
        buf.writeShort(msg.keys);
    }

    public static R6DroneControlMessage decode(FriendlyByteBuf buf) {
        return new R6DroneControlMessage(buf.readShort());
    }

    public static void handle(R6DroneControlMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.MONITOR.get())) return;
            var tag = stack.getOrCreateTag();
            if (!tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) return;

            String linked = tag.getString(MonitorItem.LINKED_DRONE);
            R6DroneEntity drone = R6DroneEntity.findDrone(player.level(), linked);
            if (drone != null) {
                drone.processInput(msg.keys);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
