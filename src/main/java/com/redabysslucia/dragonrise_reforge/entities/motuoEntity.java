package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public class motuoEntity extends NightVisionVehicle {

        public motuoEntity(EntityType<motuoEntity> type, Level world) {
                super(type, world);
        }

        public Vec3 deltaMovement0;

        @Override
        public void baseTick() {
            deltaMovement0 = getDeltaMovement();
            super.baseTick();
        }

        @Override
        public void bounceHorizontal(@NotNull Direction direction) {
            for (Entity entity: getPassengers()) {
                if(entity != null){
                    entity.stopRiding();
                    double speed = deltaMovement0.length();
                    Vec3 dir = deltaMovement0.normalize().add(getUpVec(1).scale(0.6));
                    Mod.queueServerWork(1,()->{
                        if(entity instanceof Player player && player.level().isClientSide){
                            player.setDeltaMovement(dir.normalize().scale(speed));
                        } else {
                            entity.setDeltaMovement(dir.normalize().scale(speed));
                        }
                    });

                }
            }
            super.bounceHorizontal(direction);
        }

        @Override
        public DamageModifier getDamageModifier() {
                return super.getDamageModifier()
                        .custom((source, damage) -> getSourceAngle(source, 0.05f) * damage);
        }

    @Override
    public ResourceLocation getNightVisionShader() {
        return new ResourceLocation("shaders/post/night-vision-wp.json");
    }
}
