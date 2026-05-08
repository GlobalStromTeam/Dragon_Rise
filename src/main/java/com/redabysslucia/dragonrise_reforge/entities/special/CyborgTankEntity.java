package com.redabysslucia.dragonrise_reforge.entities.special;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.mojang.math.Axis;
import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Matrix4d;
import org.joml.Vector4d;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class CyborgTankEntity extends NightVisionVehicle {
    public CyborgTankEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
    }

    private PlayState cannonFirePredicate(AnimationState<CyborgTankEntity> event) {
        if (getShootAnimationTimer(0, 0) > 0) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.cyborg_tank.fire"));
        }
        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.cyborg_tank.idle"));
    }

    private PlayState coaxFirePredicate(AnimationState<CyborgTankEntity> event) {
        if (getShootAnimationTimer(0, 1) > 0) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.cyborg_tank.fire_coax"));
        }
        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.cyborg_tank.idle"));
    }

    private PlayState stationFirePredicate(AnimationState<CyborgTankEntity> event) {
        if (getShootAnimationTimer(1, 0) > 0) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.cyborg_tank.fire_station"));
        }
        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.cyborg_tank.idle"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
        data.add(new AnimationController<>(this, "coax", 0, this::coaxFirePredicate));
        data.add(new AnimationController<>(this, "station", 0, this::stationFirePredicate));
    }

    @Override
    public ResourceLocation getNightVisionShader() {
        return new ResourceLocation("shaders/post/night-vision-wp.json");
    }

    @Override
    public float getTurretMaxHealth() {
        return 420;
    }

    @Override
    public float getWheelMaxHealth() {
        return 150;
    }

    @Override
    public float getEngineMaxHealth() {
        return 150;
    }

    @Override
    public Matrix4d getGunTransform(float partialTicks) {
        Matrix4d transformT = getVehicleTransform(partialTicks);

        Matrix4d transform = new Matrix4d();
        var pos = this.getPassengerWeaponStationPosition();
        Vector4d worldPosition = transformPosition(
                transform,
                pos.x,
                pos.y,
                pos.z
        );

        transformT.translate(worldPosition.x, worldPosition.y, worldPosition.z);
        transformT.rotate(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, this.getGunYRotO(), this.getGunYRot())));
        return transformT;
    }

    @Override
    public Matrix4d getPassengerWeaponStationBarrelTransform(float partialTicks) {
        Matrix4d transformG = getGunTransform(partialTicks);

        Matrix4d transform = new Matrix4d();
        var pos = this.getPassengerWeaponStationBarrelPosition();
        Vector4d worldPosition = transformPosition(
                transform,
                pos.x,
                pos.y,
                pos.z
        );

        transformG.translate(worldPosition.x, worldPosition.y, worldPosition.z);

        float a = this.getGunYRot(partialTicks);
        float r = (Mth.abs(a) - 90f) / 90f;
        float r2;

        if (Mth.abs(a) <= 90f) {
            r2 = a / 90f;
        } else {
            if (a < 0) {
                r2 = -(180f + a) / 90f;
            } else {
                r2 = (180f - a) / 90f;
            }
        }

        float x = Mth.lerp(partialTicks, this.getGunXRotO(), this.getGunXRot());
        float xV = Mth.lerp(partialTicks, this.xRotO, this.getXRot());
        float z = Mth.lerp(partialTicks, this.getPrevRoll(), this.getRoll());

        transformG.rotate(Axis.XP.rotationDegrees(x + r * xV + r2 * z));
        return transformG;
    }

    @Override
    public void adjustWeaponControllerAngle() {
        float ySpeed = getPassengerWeaponYSpeed();
        float xSpeed = getPassengerWeaponXSpeed();

        Entity entity = this.getNthEntity(getPassengerWeaponStationControllerIndex());

        float diffY = 0;
        float diffX = 0;
        //float speed = 1;

        if (entity instanceof Player) {
            float gunAngle = -Mth.wrapDegrees(entity.getYHeadRot() - this.getYRot());
            diffY = Mth.wrapDegrees(gunAngle - getGunYRot());
            diffX = Mth.wrapDegrees(entity.getXRot() - this.getGunXRot());
            turretTurnSound(diffX, diffY, 0.95f);
            //speed = 0;
        }

        this.setGunXRot(this.getGunXRot() + Mth.clamp(0.95f * diffX, -xSpeed, xSpeed));
        this.setGunYRot(this.getGunYRot() + Mth.clamp(0.9f * diffY, -ySpeed, ySpeed));
    }
}
