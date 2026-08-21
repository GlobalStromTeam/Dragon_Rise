package com.redabysslucia.dragonrise_reforge.client;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModKeyMappings;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.redabysslucia.dragonrise_reforge.network.message.R6DroneControlMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * 侦察无人车客户端输入处理器：
 * 玩家手持监控平板激活遥控本项目无人车时，读取 SBW 按键绑定（WASD/空格/Ctrl）
 * 组装位标志，按键变化时发送 R6DroneControlMessage 到服务器。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class R6DroneClientHandler {

    private static short lastKeys = -1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            lastKeys = -1;
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
            lastKeys = -1;
            return;
        }
        if (R6DroneEntity.findDrone(mc.level, tag.getString(MonitorItem.LINKED_DRONE)) == null) {
            lastKeys = -1;
            return;
        }

        short keys = 0;
        if (ModKeyMappings.MOVE_LEFT.isDown()) keys |= 0b000000001;
        if (ModKeyMappings.MOVE_RIGHT.isDown()) keys |= 0b000000010;
        if (ModKeyMappings.MOVE_FORWARD.isDown()) keys |= 0b000000100;
        if (ModKeyMappings.MOVE_BACKWARD.isDown()) keys |= 0b000001000;
        if (ModKeyMappings.MOVE_SPACE.isDown()) keys |= 0b000010000;
        if (ModKeyMappings.MOVE_CTRL.isDown()) keys |= 0b100000000;

        if (keys != lastKeys) {
            lastKeys = keys;
            ModNetwork.PACKET_HANDLER.sendToServer(new R6DroneControlMessage(keys));
        }
    }
}
