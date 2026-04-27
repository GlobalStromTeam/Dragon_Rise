package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import com.atsuishio.superbwarfare.tools.OBB.Part;
import com.redabysslucia.dragonrise_reforge.init.ModSpeedSounds;
import com.redabysslucia.dragonrise_reforge.utils.SpeedSoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Math;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class NightVisionVehicle extends GeoVehicleEntity implements INightVisionVehicle {

    public static final EntityDataAccessor<Boolean> NV_Enable = SynchedEntityData.defineId(NightVisionVehicle.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> TVG_Enable = SynchedEntityData.defineId(NightVisionVehicle.class, EntityDataSerializers.BOOLEAN);
    
    // 存储实体的光源方块位置和放置时间
    private static final Map<NightVisionVehicle, LightInfo> lightInfoMap = new HashMap<>();
    
    // 用于跟踪需要删除的光源方块
    private int lightRemovalTimer = 0;
    // 耕地减速检测计数器
    private int farmlandCheckTimer = 0;

    // 耕地减速检测方法
    private void checkFarmlandSlowdown() {
        // 计算载具底部位置，向上偏移0.5格以适应耕地方块高度
        net.minecraft.world.phys.Vec3 vehiclePos = this.position();
        net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(
            (int) Math.floor(vehiclePos.x()),
            (int) Math.floor(vehiclePos.y() - 0.5), // 向上偏移0.5格
            (int) Math.floor(vehiclePos.z())
        );
        net.minecraft.world.level.block.state.BlockState blockState = this.level().getBlockState(blockPos);
        if (blockState.is(net.minecraft.world.level.block.Blocks.FARMLAND)) {
            // 获取当前动力值
            float currentPower = this.entityData.get(com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.POWER);
            if (org.joml.Math.abs(currentPower) > 0.3f) {
                // 减速50%
                this.entityData.set(com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.POWER, currentPower * 0.5f);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(NV_Enable, false);
        this.entityData.define(TVG_Enable, false);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(NV_Enable, compound.getBoolean("NV_Enable"));
        this.entityData.set(TVG_Enable, compound.getBoolean("TVG_Enable"));
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("NV_Enable", this.entityData.get(NV_Enable));
        compound.putBoolean("TVG_Enable", this.entityData.get(TVG_Enable));
    }

    @Override
    public boolean getNVEnable() {
        return this.entityData.get(NV_Enable);
    }

    @Override
    public void setNVEnable(boolean enable) {
        this.entityData.set(NV_Enable, enable);
        // 确保夜视和热成像互斥
        if (enable) {
            this.entityData.set(TVG_Enable, false);
        }
    }
    
    @Override
    public boolean getTVGEnable() {
        return this.entityData.get(TVG_Enable);
    }
    
    @Override
    public void setTVGEnable(boolean enable) {
        this.entityData.set(TVG_Enable, enable);
        // 确保热成像和夜视互斥
        if (enable) {
            this.entityData.set(NV_Enable, false);
        }
    }
    
    @Override
    public ResourceLocation getNightVisionShader() {
        return new ResourceLocation("minecraft", "shaders/post/night-vision-wp.json");
    }
    
    @Override
    public ResourceLocation getThermalVisionShader() {
        return new ResourceLocation("minecraft", "shaders/post/thermal-vision-wp.json");
    }

    @Override
    public void vehicleShoot(LivingEntity living, String weaponName) {
        super.vehicleShoot(living, weaponName);
        // 处理炮口火光效果
        handleTurretFireLight(1); // 传入1表示正在开火
        // 设置删除计时器
        lightRemovalTimer = 1; // 1 tick后删除
    }

    @Override
    public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
        super.vehicleShoot(living, uuid, targetPos);
        // 处理炮口火光效果
        handleTurretFireLight(1); // 传入1表示正在开火
        // 设置删除计时器
        lightRemovalTimer = 1; // 1 tick后删除
    }

    @Override
    public void tick() {
        super.tick();
        
        // 耕地减速检测（每10 tick检测一次）
        farmlandCheckTimer++;
        if (farmlandCheckTimer >= 10) {
            checkFarmlandSlowdown();
            farmlandCheckTimer = 0;
        }
        
        // 处理光源方块删除
        if (lightRemovalTimer > 0) {
            lightRemovalTimer--;
            if (lightRemovalTimer == 0) {
                // 删除光源方块
                handleTurretFireLight(0); // 传入0表示停止开火
            }
        }
        
        // 检查速度并播放音效
        SpeedSoundUtil.checkSpeedAndPlaySound(this, ModSpeedSounds.PLANE_HIGH_SPEED.get());
    }

    /**
     * 处理坦克主武器开火时的炮口火光效果
     * @param shootTimer 开火动画计时器
     */
    protected void handleTurretFireLight(int shootTimer) {
        Level level = this.level();
        if (!level.isClientSide()) {
            // 检查是否正在开火
            if (shootTimer > 0) {
                // 查找Turret obb
                for (OBB obb : this.getOBBs()) {
                    if (obb.part() == Part.TURRET) {
                        // 获取Turret obb的中心位置
                        Vec3 obbCenter = OBB.vector3dToVec3(obb.center());
                        // 获取OBB的旋转
                        Quaterniond rotation = obb.rotation();
                        
                        // 计算OBB的北方方向（基于OBB的旋转）
                        // 初始北方方向是(0, 0, 1)
                        Vector3d northDirection = new Vector3d(0, 0, 1);
                        // 应用OBB的旋转
                        northDirection.rotate(rotation);
                        
                        // 计算偏移后的位置：往OBB的北方偏移两格，Y轴下降一格
                        double offsetX = northDirection.x * 2;
                        double offsetY = 0;
                        double offsetZ = northDirection.z * 2;
                        
                        // 计算最终位置
                        double finalX = obbCenter.x + offsetX;
                        double finalY = obbCenter.y + offsetY;
                        double finalZ = obbCenter.z + offsetZ;
                        
                        // 转换为方块位置
                        BlockPos pos = new BlockPos((int) Math.floor(finalX), (int) Math.floor(finalY), (int) Math.floor(finalZ));

                        if (level.isEmptyBlock(pos)) {
                            level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL, 10), 3);
                            // 记录光源信息
                            lightInfoMap.put(this, new LightInfo(pos, level.getGameTime()));
                        }
                        break;
                    }
                }
            } else {
                // 删除之前的光源方块
                LightInfo lightInfo = lightInfoMap.get(this);
                if (lightInfo != null) {
                    level.removeBlock(lightInfo.pos, false);
                    lightInfoMap.remove(this);
                }
            }
        }
    }

    // 存储光源方块信息的内部类
    private static class LightInfo {
        public final BlockPos pos;
        public final long placementTime;

        public LightInfo(BlockPos pos, long placementTime) {
            this.pos = pos;
            this.placementTime = placementTime;
        }
    }

    public NightVisionVehicle(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

}