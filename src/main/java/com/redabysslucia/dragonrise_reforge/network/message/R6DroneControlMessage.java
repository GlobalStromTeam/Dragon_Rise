package com.redabysslucia.dragonrise_reforge.network.message;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.NBTTool;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 侦察无人车键盘输入消息（客户端 → 服务器）：
 * 携带 WASD/空格/冲刺 的按键位标志，服务器写入对应无人车实体。
 * 鼠标/视角无需传输 —— 服务器直接读取控制者玩家的旋转。
 */
public class R6DroneControlMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<R6DroneControlMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "r6_drone_control"));

    public static final StreamCodec<RegistryFriendlyByteBuf, R6DroneControlMessage> STREAM_CODEC =
            StreamCodec.of(R6DroneControlMessage::encode, R6DroneControlMessage::decode);

    private final short keys;

    public R6DroneControlMessage(short keys) {
        this.keys = keys;
    }

    public static void encode(RegistryFriendlyByteBuf buf, R6DroneControlMessage msg) {
        buf.writeShort(msg.keys);
    }

    public static R6DroneControlMessage decode(RegistryFriendlyByteBuf buf) {
        return new R6DroneControlMessage(buf.readShort());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(R6DroneControlMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (player == null) return;
            ItemStack stack = player.getMainHandItem();
            if (!stack.is(ModItems.MONITOR.get())) return;
            var tag = NBTTool.getTag(stack);
            if (!tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) return;

            String linked = tag.getString(MonitorItem.LINKED_DRONE);
            R6DroneEntity drone = R6DroneEntity.findDrone(player.level(), linked);
            if (drone != null) {
                drone.processInput(msg.keys);
            }
        });
    }
}
