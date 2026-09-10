package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.redabysslucia.dragonrise_reforge.entities.utils.VariableEngineVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * I.K.-战猫：与 J20VTOL 相同的可变引擎基类（固定翼 / 直升机双模式，按模式切换键垂直起降）。
 */
public class IkFightcatEntity extends VariableEngineVehicle {

    public IkFightcatEntity(EntityType<IkFightcatEntity> type, Level world) {
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
     * 固定翼模式机头防下垂（与蝗虫工程船一致）：抵消 superb updateRotation 的
     * -0.06 虚构下坠配平，静止悬浮时机头不再缓缓低垂。
     */
    @Override
    protected boolean cancelFakeNoseDropInAircraftMode() {
        return true;
    }

    /**
     * 模型带起落架骨骼与 gear_up/gear_down 动画，但载具 JSON 未配置 HasGear：
     * 代码层强制启用引擎的起落架收放逻辑（空中按空格收轮、着地自动放轮）。
     */
    @Override
    protected boolean forceGearEnabled() {
        return true;
    }

    /**
     * 动画文件命名与内容相反：gear_up 文件内容是放下、gear_down 文件内容是收起
     * （绑定姿态即放下，对比 J20VTOL 的 gear_up 转到 -90° 收起可知）。
     * 状态机据此对调播放。
     */
    @Override
    protected boolean swapGearAnimations() {
        return true;
    }
}
