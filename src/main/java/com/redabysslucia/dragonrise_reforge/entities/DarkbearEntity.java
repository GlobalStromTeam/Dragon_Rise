package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector4d;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

@SuppressWarnings("removal")
public class DarkbearEntity extends GeoVehicleEntity {

        public DarkbearEntity(EntityType<DarkbearEntity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
        }

        private PlayState cannonFirePredicate(AnimationState<DarkbearEntity> event) {
                if (getShootAnimationTimer(0, 0) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation.model.new"));
                }
                return event.setAndContinue(RawAnimation.begin().thenLoop("nothing"));
        }

        private PlayState mgFirePredicate(AnimationState<DarkbearEntity> event) {
                if (getShootAnimationTimer(1, 0) > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation.model.new3"));
                }
                return event.setAndContinue(RawAnimation.begin().thenLoop("nothing"));
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar data) {
                data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
        }

        @Override
        public Vec3 getShootPos(String weaponName, float ticks) {
                if ("subcannon".equals(weaponName)) {
                        GunData data = getGunData(weaponName);
                        if (data != null) {
                                return getSubcannonWorldPos(data.firePosition(), ticks);
                        }
                }
                return super.getShootPos(weaponName, ticks);
        }

        @Override
        public Vec3 getShootPos(Entity entity, float ticks) {
                if (entity != null && "subcannon".equals(getGunName(getSeatIndex(entity)))) {
                        GunData data = getGunData(getSeatIndex(entity));
                        if (data != null) {
                                return getSubcannonWorldPos(data.firePosition(), ticks);
                        }
                }
                return super.getShootPos(entity, ticks);
        }

        @Override
        public Vec3 getShootPosForHud(Entity entity, float ticks) {
                if (entity != null && "subcannon".equals(getGunName(getSeatIndex(entity)))) {
                        GunData data = getGunData(getSeatIndex(entity));
                        if (data != null) {
                                Vec3 pos = data.firePositionForHud();
                                if (pos == null) pos = data.firePosition();
                                return getSubcannonWorldPos(pos, ticks);
                        }
                }
                return super.getShootPosForHud(entity, ticks);
        }

        private Vec3 getSubcannonWorldPos(Vec3 localPos, float ticks) {
                Matrix4d transform = new Matrix4d(getTurretTransform(ticks));
                float pitch = Mth.lerp(ticks, getTurretXRotO(), getTurretXRot());
                transform.rotate(Axis.XP.rotationDegrees(pitch));

                Vector4d worldPos = transformPosition(transform, localPos.x, localPos.y, localPos.z);
                return new Vec3(worldPos.x, worldPos.y, worldPos.z);
        }
}
