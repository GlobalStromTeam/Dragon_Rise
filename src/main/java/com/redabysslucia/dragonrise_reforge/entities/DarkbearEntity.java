package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
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

@SuppressWarnings("removal")
public class DarkbearEntity extends VehicleEntity {

        public DarkbearEntity(EntityType<DarkbearEntity> type, Level world) {
                super(type, world);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((entity, source, damage) -> getSourceAngle(source, 0.3f) * damage);
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
