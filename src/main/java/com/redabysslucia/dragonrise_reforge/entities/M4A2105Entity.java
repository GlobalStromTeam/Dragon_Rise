package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("removal")
public class M4A2105Entity extends SyncCameraVehicle implements IVehicleBackground {

    public M4A2105Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.3f) * damage);
    }

//    private PlayState cannonFirePredicate(AnimationState<PANZER4Entity> event) {
//        if (getShootAnimationTimer(0, 0) > 0) {
//            return event.setAndContinue(RawAnimation.begin().thenPlay("ztq15.fire.animation"));
//        }
//        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.ztq15.idle"));
//    }
//
//    @Override
//    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
//        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
//    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ResourceLocation getBackgroundTexture() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return null;

        var seatIndex = getSeatIndex(player);
        // 一号位背景
        if (seatIndex == 0) {
            return new ResourceLocation(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos2.png");
        }
        // 二号位背景
        else if (seatIndex == 1) {
            return new ResourceLocation(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/shermancroos.png");
        }
        return null;
    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 100;
    }

    @Override
    public float getWheelMaxHealth() {
        return 100;
    }

    @Override
    public float getEngineMaxHealth() {
        return 150;
    }

}
