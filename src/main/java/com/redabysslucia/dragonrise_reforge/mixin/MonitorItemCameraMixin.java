package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 进入遥控时直接进<b>第一人称</b>，而不是官方默认的"第三人称背面"。
 * <p>
 * 官方 {@code MonitorItem.use} 在开启 Using 时会把相机强制切成 {@code THIRD_PERSON_BACK}
 * （并记下原相机类型，退出时还原）。对 SBW 自家无人机没毛病，但对本机有两个后果：
 * <ol>
 *   <li>官方战斗机 HUD 按 {@code cameraType} 选"第一/第三人称"那套元素，
 *       于是进操控先看到的是第三人称 HUD，要按 F5 循环到第一人称才变战斗机 HUD；</li>
 *   <li>而相机本身（SBW 把 FIRST_PERSON 与 THIRD_PERSON_BACK 同等对待）已经在机身内，
 *       画面与 HUD 就不是一套，看着别扭。</li>
 * </ol>
 * 本 mixin 在该方法返回后补一步：如果这次是遥控本模组的固定翼，就把相机改成第一人称
 * ——即"机头光电球第一人称 + 第一人称战斗机 HUD"，F5 之后照样可以切到机外视角看自己的飞机。
 */
@Mixin(MonitorItem.class)
public class MonitorItemCameraMixin {

    @Inject(method = "use", at = @At("RETURN"))
    private void dragonrise$firstPersonForRemoteAircraft(Level level, Player player, InteractionHand hand,
                                                         CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!level.isClientSide()) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.MONITOR.get())) {
            return;
        }
        var tag = stack.getTag();
        if (tag == null || !tag.getBoolean("Using") || !tag.getBoolean("Linked")) {
            return;
        }
        var drone = EntityFindUtil.findDrone(level, tag.getString("LinkedDrone"));
        if (!(drone instanceof ShuangweixieEntity)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }
}
