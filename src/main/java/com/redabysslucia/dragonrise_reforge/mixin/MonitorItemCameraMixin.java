package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.NBTTool;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.entities.AbstractRemotePilotDrone;
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
 * 进入遥控时决定<b>用哪一套 HUD</b>：把相机类型钉到与机型配置一致的那一档。
 * <p>
 * 官方 {@code MonitorItem.use} 开启 Using 时会把相机强制切成 {@code THIRD_PERSON_BACK}
 * （并记下原类型，退出时还原）。SBW 的 {@code AircraftHud}／{@code VehicleCrosshairOverlay}
 * 都是靠 {@code cameraType} 选"第一人称 / 第三人称"两套元素的 ——
 * 而遥控相机本身由 {@code RemoteAircraftCameraMixin} 在 {@code Camera.setup} 的 HEAD 接管，
 * 它把 {@code FIRST_PERSON} 与 {@code THIRD_PERSON_BACK} 当作同一档（都放机位），
 * 所以<b>相机类型只影响 HUD 样式，不影响画面机位</b>。
 * <p>
 * 于是这里按载具数据 {@code EngineInfo.ThirdPersonHud} 选：
 * <ul>
 *   <li>{@code true} → 钉成 {@code THIRD_PERSON_BACK}：小圆环准星 + 命中点准星 + 武器信息
 *       （本模组的察打无人机用这套，机身外观和第一人称那套座舱 HUD 不搭）；</li>
 *   <li>{@code false}（缺省）→ 钉成 {@code FIRST_PERSON}：座舱式 HUD（双尾蝎仍走这条）。</li>
 * </ul>
 * 两种情况下 F5 都还能临时切视角，下次右键平板会重新按配置钉回来。
 */
@Mixin(MonitorItem.class)
public class MonitorItemCameraMixin {

    @Inject(method = "use", at = @At("RETURN"))
    private void dragonrise$cameraTypeForRemoteAircraft(Level level, Player player, InteractionHand hand,
                                                        CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!level.isClientSide()) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.MONITOR.get())) {
            return;
        }
        var tag = NBTTool.getTag(stack);
        if (tag == null || !tag.getBoolean("Using") || !tag.getBoolean("Linked")) {
            return;
        }
        var drone = EntityFindUtil.findDrone(level, tag.getString("LinkedDrone"));
        if (!(drone instanceof AbstractRemotePilotDrone aircraft)) {
            return;
        }

        // 相机类型在这两档之间只影响 HUD 样式；机位与朝向由 RemoteAircraftCameraMixin 统一接管
        CameraType want = aircraft.dr$thirdPersonHud()
                ? CameraType.THIRD_PERSON_BACK
                : CameraType.FIRST_PERSON;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() != want) {
            mc.options.setCameraType(want);
        }
    }
}
