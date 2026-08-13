package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public class motuoEntity extends GeoVehicleEntity {

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

        double currentSpeed = deltaMovement0.length();

        boolean isDownwardImpact = direction == Direction.DOWN;

        if (isDownwardImpact) {
            if (currentSpeed > 2.0) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.3));
            }
            super.bounceHorizontal(direction);
            return;
        }

        if (currentSpeed < 0.5) {
            super.bounceHorizontal(direction);
            return;
        }

        if (this.level() instanceof ServerLevel) {
            for (Entity entity : getPassengers()) {
                double speed = getDeltaMovement().length();
                if (speed > 0.4) {
                    entity.stopRiding();
                    Vec3 dir = deltaMovement0.normalize().add(getUpVec(1).scale(0.6));
                    Mod.queueServerWork(1, () -> {
                        if (entity instanceof Player player) {
                            player.setDeltaMovement(dir.normalize().scale(speed));
                        } else {
                            entity.setDeltaMovement(dir.normalize().scale(speed));
                        }
                    });

                }
            }
        }
        super.bounceHorizontal(direction);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.05f) * damage);
    }


}