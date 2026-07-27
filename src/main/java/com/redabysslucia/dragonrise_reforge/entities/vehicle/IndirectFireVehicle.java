package com.redabysslucia.dragonrise_reforge.entities.vehicle;

import com.redabysslucia.dragonrise_reforge.firecontrol.FireControlComputation;
import com.redabysslucia.dragonrise_reforge.firecontrol.FireControlStatus;
import com.redabysslucia.dragonrise_reforge.firecontrol.TrajectoryMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public interface IndirectFireVehicle {
    boolean isFireControlActive();

    BlockPos getFireControlTarget();

    int getFireControlRadius();

    TrajectoryMode getFireControlTrajectory();

    FireControlStatus getFireControlStatus();

    FireControlComputation getFireControlComputation();

    boolean applyFireControl(BlockPos target, int radius, TrajectoryMode mode, Entity actor);

    void clearFireControl(Entity actor);

    boolean isFireControlTakeoverEnabled();

    void setFireControlTakeover(boolean enabled, Entity actor);
}
