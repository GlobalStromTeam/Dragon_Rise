package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * CH-53E 超种马重型运输直升机。
 * 载具性能（EngineInfo / OBB / Seats）全部来自 data/dragonrise_reforge/sbw/vehicles/ch53e.json，
 * 模型是 SBM 的 poly_mesh（assets/.../models/bedrock/vehicle/ch53e.geo.json）。
 */
@SuppressWarnings("removal")
public class Ch53EEntity extends VehicleEntity {

    public Ch53EEntity(EntityType<Ch53EEntity> type, Level world) {
        super(type, world);
    }
}
