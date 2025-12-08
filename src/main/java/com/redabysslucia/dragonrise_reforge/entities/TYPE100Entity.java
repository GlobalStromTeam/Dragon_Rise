package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;

import java.util.UUID;

import static com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity.BARREL_ANIM;

@SuppressWarnings("removal")
public class TYPE100Entity extends NightVisionVehicle {

        public TYPE100Entity(EntityType<?> pEntityType, Level pLevel) {
                super(pEntityType, pLevel);
        }

        @Override
        public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
                super.vehicleShoot(living, uuid, targetPos);
                beforeShoot(living);
        }
        public void beforeShoot(LivingEntity living) {
                getGunData("Cannon");
                if (this.getGunData("Cannon") == null) {
                        return;
                }
                if (living.level() instanceof ServerLevel level ) {
                        ParticleTool.spawnBigCannonMuzzleParticles(
                                getShootVec("Cannon", 1),
                                getShootPos("Cannon", 1), level, this);
                }
        }

        @Override
        public ResourceLocation getNightVisionShader() {
                return new ResourceLocation("shaders/post/night-vision-wp.json");
        }
}
