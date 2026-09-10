package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.redabysslucia.dragonrise_reforge.entities.utils.VariableEngineVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * I.K.-蝗虫工程船：与 J20VTOL 相同的可变引擎基类（固定翼 / 直升机双模式，按模式切换键垂直起降）。
 */
public class IkLocustEntity extends VariableEngineVehicle {

    public IkLocustEntity(EntityType<IkLocustEntity> type, Level world) {
        super(type, world);
        this.setEngineTypeList(List.of(EngineType.AIRCRAFT, EngineType.HELICOPTER));
    }

    /**
     * 固定翼模式：无重力、无升力（纯推力矢量飞行）；
     * 直升机模式：重力（JSON 0.06）与悬停升力正常生效。
     */
    @Override
    protected boolean noGravityAndLiftInAircraftMode() {
        return true;
    }

    /**
     * 固定翼模式机头防下垂（抵消 superb updateRotation 的 -0.06 虚构下坠配平），
     * 具体实现由 {@link VariableEngineVehicle} 统一提供。
     */
    @Override
    protected boolean cancelFakeNoseDropInAircraftMode() {
        return true;
    }
}
