package com.redabysslucia.dragonrise_reforge.client;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModKeyMappings;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.sound.R6DroneLoopSoundInstance;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.redabysslucia.dragonrise_reforge.network.message.R6DroneControlMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 侦察无人车/攻击无人机客户端输入与循环音效处理器：
 * 1) 玩家手持监控平板激活遥控本项目无人车时，读取 SBW 按键绑定（WASD/空格/Ctrl）
 *    组装位标志，按键变化时发送 R6DroneControlMessage 到服务器。
 * 2) 遥控时播放无人车移动循环音（moving 普通 / fast 冲刺，客户端循环 SoundInstance），
 *    移动停止或退出遥控时停止。
 * 3) 左键（攻击键）通过 InputEvent.MouseButton 捕获 —— 遥控视角下 keyAttack.isDown()
 *    会被原版攻击逻辑消耗，MouseButton 事件更可靠。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class R6DroneClientHandler {

    private static short lastKeys = -1;

    /** 当前正在播放的移动循环音实例（null = 未播放） */
    private static R6DroneLoopSoundInstance playingLoop;

    /** 左键按住状态（InputEvent.MouseButton 维护） */
    private static boolean attackHeld;

    /** 左键（0 = 鼠标左键）按下/释放事件：仅遥控时记录状态 */
    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        ItemStack stack = mc.player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) return;
        var tag = stack.getTag();
        if (tag == null || !tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) return;
        if (R6DroneEntity.findDrone(mc.level, tag.getString(MonitorItem.LINKED_DRONE)) == null) return;
        if (event.getButton() == 0) {
            attackHeld = event.getAction() != 0; // 1=按下, 0=释放
            event.setCanceled(true); // 拦截原版攻击，避免挥动手臂/破坏方块
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            stopLoop(mc);
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            lastKeys = -1;
            stopLoop(mc);
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
            lastKeys = -1;
            stopLoop(mc);
            return;
        }
        R6DroneEntity drone = R6DroneEntity.findDrone(mc.level, tag.getString(MonitorItem.LINKED_DRONE));
        if (drone == null) {
            lastKeys = -1;
            stopLoop(mc);
            return;
        }

        // ---- 输入同步 ----
        short keys = 0;
        if (ModKeyMappings.MOVE_LEFT.isDown()) keys |= 0b000000001;
        if (ModKeyMappings.MOVE_RIGHT.isDown()) keys |= 0b000000010;
        if (ModKeyMappings.MOVE_FORWARD.isDown()) keys |= 0b000000100;
        if (ModKeyMappings.MOVE_BACKWARD.isDown()) keys |= 0b000001000;
        if (ModKeyMappings.MOVE_SPACE.isDown()) keys |= 0b000010000;
        if (attackHeld) keys |= 0b000100000; // 左键：攻击无人机开火
        if (ModKeyMappings.MOVE_CTRL.isDown()) keys |= 0b100000000;

        if (keys != lastKeys) {
            lastKeys = keys;
            ModNetwork.PACKET_HANDLER.sendToServer(new R6DroneControlMessage(keys));
        }

        // ---- 移动循环音 ----
        boolean moving = (keys & 0b000001111) != 0; // 前进/后退/左/右 任一
        boolean sprinting = (keys & 0b100000000) != 0; // Ctrl
        updateLoop(mc, drone, moving, sprinting);
    }

    /** 根据移动/冲刺状态播放或停止循环音 */
    private static void updateLoop(Minecraft mc, R6DroneEntity drone, boolean moving, boolean sprinting) {
        SoundManager sm = mc.getSoundManager();
        boolean shouldPlay = moving;
        boolean wantFast = sprinting;

        if (!shouldPlay) {
            stopLoop(mc);
            return;
        }

        // 需要 fast 但当前不是 fast（或未播放）→ 切换
        if (wantFast && (playingLoop == null || !playingLoop.isFast())) {
            stopLoop(mc);
            playingLoop = new R6DroneLoopSoundInstance(drone, mc, true);
            sm.play(playingLoop);
        }
        // 需要 moving 但当前不是 moving（或未播放）→ 切换
        else if (!wantFast && (playingLoop == null || playingLoop.isFast())) {
            stopLoop(mc);
            playingLoop = new R6DroneLoopSoundInstance(drone, mc, false);
            sm.play(playingLoop);
        }
        // 已播放且类型正确 → 无需操作（循环音持续，tick 内自动跟随位置）
    }

    private static void stopLoop(Minecraft mc) {
        if (playingLoop != null) {
            mc.getSoundManager().stop(playingLoop);
            playingLoop = null;
        }
    }
}
