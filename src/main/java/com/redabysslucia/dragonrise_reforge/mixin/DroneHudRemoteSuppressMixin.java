package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.DroneHudOverlay;
import com.atsuishio.superbwarfare.client.overlay.RenderContext;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 遥控双尾蝎时不再画官方无人机 HUD。
 * <p>
 * 本机是 {@code HudType: "@Aircraft"} 的固定翼，遥控 HUD 改由
 * {@code RemoteAircraftHudOverlay} 渲染战斗机 HUD；两套 HUD 叠在一起会互相压字，
 * 所以这里把无人机 HUD 针对本机关掉（其余 SBW 无人机的 HUD 行为不变）。
 */
@Mixin(value = DroneHudOverlay.class, remap = false)
public class DroneHudRemoteSuppressMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void dragonrise$suppressForOurAircraft(RenderContext context, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        ItemStack stack = mc.player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
            return;
        }
        DroneEntity drone = EntityFindUtil.findDrone(mc.player.level(), tag.getString(MonitorItem.LINKED_DRONE));
        if (drone instanceof ShuangweixieEntity) {
            ci.cancel();
        }
    }
}
