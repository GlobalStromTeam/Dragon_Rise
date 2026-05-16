package com.redabysslucia.dragonrise_reforge.entities.utils;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public abstract class SyncCameraVehicle extends FireLightVisionVehicle {
    
    private final Random random = new Random();
    private int shakeCooldown = 0;
    private int shakeDuration = 0;
    private float currentShakeIntensity = 0;
    private float shakePhase = 0;
    
    private float prevShakePitch = 0;
    private float prevShakeRoll = 0;
    private float shakePitch = 0;
    private float shakeRoll = 0;
    
    // 保存载具基础旋转（不带抖动）
    private float vehicleBaseXRot = 0;
    private float vehicleBaseRoll = 0;

    public SyncCameraVehicle(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        resetShake();
    }

    @Override
    public void tick() {
        super.tick();
        
        // 先保存上一帧的抖动值
        prevShakePitch = shakePitch;
        prevShakeRoll = shakeRoll;
        
        // 更新抖动
        updateVehicleShake();
        
        // 应用抖动到载具旋转
        applyShakeToVehicle();
    }

    private void updateVehicleShake() {
        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();
        
        if (speed <= 0.1) {
            if (shakeDuration > 0) {
                shakeDuration--;
                calculateShake();
            } else {
                shakePitch *= 0.9f;
                shakeRoll *= 0.9f;
                if (Math.abs(shakePitch) < 0.01f) shakePitch = 0;
                if (Math.abs(shakeRoll) < 0.01f) shakeRoll = 0;
            }
            return;
        }
        
        if (shakeDuration > 0) {
            shakeDuration--;
            shakePhase += 0.3f;
            calculateShake();
            
            if (shakeDuration == 0) {
                resetShake();
            }
        } else {
            shakeCooldown--;
            shakePitch *= 0.9f;
            shakeRoll *= 0.9f;
            if (Math.abs(shakePitch) < 0.01f) shakePitch = 0;
            if (Math.abs(shakeRoll) < 0.01f) shakeRoll = 0;
            
            if (shakeCooldown <= 0) {
                startShake();
            }
        }
    }

    private void applyShakeToVehicle() {
        // 计算抖动的变化量
        float deltaPitch = shakePitch - prevShakePitch;
        float deltaRoll = shakeRoll - prevShakeRoll;
        
        // 应用到载具旋转
        this.setXRot(this.getXRot() + deltaPitch);
        this.setRoll(this.getRoll() + deltaRoll);
    }

    private void startShake() {
        // 先计算速度因子（范围约0.5-2.0，放大高速时的效果）
        float speedFactor = (float) Math.min(getDeltaMovement().length() * 1.0, 2);
        
        // 基础抖动强度：0.6-1.2
        currentShakeIntensity = (random.nextFloat() * 0.6f + 0.6f) * speedFactor;
        
        // 抖动持续时间与速度成正比（基础6-10 tick）
        // 慢速时：6-10 tick（0.3-0.5秒）
        // 快速时：12-20 tick（0.6-1.0秒）
        int durationBase = random.nextInt(5) + 6;
        int durationBonus = (int)(speedFactor * 6); // 根据速度增加0-12 tick
        shakeDuration = durationBase + durationBonus;
        
        shakePhase = random.nextFloat() * (float) Math.PI * 2;
    }

    private void resetShake() {
        // 抖动间隔：0.2-0.6秒（4-12 tick）
        shakeCooldown = random.nextInt(9) + 4;
        shakeDuration = 0;
        currentShakeIntensity = 0;
    }

    private void calculateShake() {
        float bumpX = (float) Math.sin(shakePhase) * currentShakeIntensity;
        
        // 根据移动方向调整抖动方向
        float direction = getMovementDirection();
        float bumpY = (float) Math.cos(shakePhase * 1.5f) * currentShakeIntensity * 2.0f * direction;
        
        shakeRoll = bumpX * 0.5f;
        shakePitch = bumpY;
    }
    
    private float getMovementDirection() {
        Vec3 motion = this.getDeltaMovement();
        double yaw = this.getYRot() * (Math.PI / 180.0);
        
        // 计算前进方向的向量
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        
        // 计算实际移动方向与前进方向的点积
        double dotProduct = motion.x * forwardX + motion.z * forwardZ;
        
        // 前进时返回1，后退时返回-1
        return dotProduct > 0 ? 1.0f : -1.0f;
    }
    
    public float getInterpolatedShakePitch(float partialTicks) {
        return net.minecraft.util.Mth.lerp(partialTicks, prevShakePitch, shakePitch);
    }
    
    public float getInterpolatedShakeRoll(float partialTicks) {
        return net.minecraft.util.Mth.lerp(partialTicks, prevShakeRoll, shakeRoll);
    }
}