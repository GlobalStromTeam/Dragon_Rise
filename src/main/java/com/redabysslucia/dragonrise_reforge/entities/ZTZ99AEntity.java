package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class ZTZ99AEntity extends NightVisionVehicle implements IVehicleBackground {

    private final Float[][] PitchAdjustments = {
            {180f, 180f, 180f, 4f, -4f},
            {-180f, -180f, 180f, 4f, -4f},
    };

    public ZTZ99AEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
    }

    private PlayState cannonFirePredicate(AnimationState<ZTZ99AEntity> event) {
        if (getShootAnimationTimer(0, 0) > 0) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.ztz99a.fire"));
        }
        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.ztz99a.idle"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
    }

    @Override
    public ResourceLocation getNightVisionShader() {
        return new ResourceLocation("shaders/post/night-vision-wp.json");
    }

//    @Override
//    public int getTrackAnimationLength() {
//        return 80;
//    }
//
//    @Override
//    public float getTurretMaxHealth() {
//        return 100;
//    }
//
//    @Override
//    public float getWheelMaxHealth() {
//        return 100;
//    }
//
//    @Override
//    public float getEngineMaxHealth() {
//        return 150;
//    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getBackgroundTexture() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return null;

        var seatIndex = getSeatIndex(player);
        // 一号位背景
        if (seatIndex == 0) {
            return new ResourceLocation(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos3.png");
        }
        // 二号位背景
        else if (seatIndex == 1) {
            return new ResourceLocation(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos2.png");
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderBackground() {
        // 只在一号位和二号位显示背景
        var mc = net.minecraft.client.Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return false;

        var seatIndex = getSeatIndex(player);
        // 一号位是索引0，二号位是索引1
        return seatIndex == 0 || seatIndex == 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getBackgroundAlpha() {
        // 控制背景透明度 (0.0f - 1.0f)
        return 1.0f;
    }
}
