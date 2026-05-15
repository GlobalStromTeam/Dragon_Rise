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
        shakeDuration = random.nextInt(3) + 2;
        currentShakeIntensity = (random.nextFloat() * 0.4f + 0.6f) * (float) Math.min(getDeltaMovement().length() * 0.5, 1);
        shakePhase = random.nextFloat() * (float) Math.PI * 2;
    }

    private void resetShake() {
        shakeCooldown = random.nextInt(5) + 2;
        shakeDuration = 0;
        currentShakeIntensity = 0;
    }

    private void calculateShake() {
        float bumpX = (float) Math.sin(shakePhase) * currentShakeIntensity;
        float bumpY = (float) Math.cos(shakePhase * 1.5f) * currentShakeIntensity * 0.5f;
        
        shakeRoll = bumpX * 0.5f;
        shakePitch = bumpY;
    }
    
    public float getInterpolatedShakePitch(float partialTicks) {
        return net.minecraft.util.Mth.lerp(partialTicks, prevShakePitch, shakePitch);
    }
    
    public float getInterpolatedShakeRoll(float partialTicks) {
        return net.minecraft.util.Mth.lerp(partialTicks, prevShakeRoll, shakeRoll);
    }
}