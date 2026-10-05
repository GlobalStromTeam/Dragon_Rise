package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * SPG-9 无后坐力炮（73mm，苏制牵引/三脚架式）：EngineType=Fixed 的固定武器，
 * 由 SPG9Deployer 部署（物品 id dragonrise_reforge:spg9），与 DShK / M2 等固定武器同类。
 */
public class Spg9Entity extends DragonriseVehicleBase {

    public Spg9Entity(EntityType<?> type, Level world) {
        super(type, world);
    }
}
