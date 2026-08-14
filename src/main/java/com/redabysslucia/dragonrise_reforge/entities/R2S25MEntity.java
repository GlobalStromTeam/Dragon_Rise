package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.utils.GeoBasedParticleUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class R2S25MEntity extends VehicleEntity {

    public R2S25MEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount % 1 == 0 && hasPlayerOperator()) {
            GeoBasedParticleUtil.spawnParticlesFromManualPosition(this, -27, 25, 47);
        }
    }
/**
     * 检查是否有玩家在操作车辆
     * @return 如果有玩家在操作返回true，否则返回false
     */
    private boolean hasPlayerOperator() {
        // 检查是否有乘客
        if (!this.getPassengers().isEmpty()) {
            // 检查第一个乘客是否是玩家
            return this.getPassengers().get(0) instanceof net.minecraft.world.entity.player.Player;
        }
        return false;
    }




}