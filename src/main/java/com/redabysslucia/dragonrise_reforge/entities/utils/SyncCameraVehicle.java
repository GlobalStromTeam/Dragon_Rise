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

    public SyncCameraVehicle(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        resetShake();
    }

    @Override
    public void tick() {
        super.tick();
        
        updateVehicleShake();
    }

    private void updateVehicleShake() {
        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();
        
        if (speed <= 0.1) {
            if (shakeDuration > 0) {
                shakeDuration--;
                applyShake();
            }
            return;
        }
        
        if (shakeDuration > 0) {
            shakeDuration--;
            shakePhase += 0.3f;
            applyShake();
            
            if (shakeDuration == 0) {
                resetShake();
            }
        } else {
            shakeCooldown--;
            
            if (shakeCooldown <= 0) {
                startShake();
            }
        }
    }

    private void startShake() {
        shakeDuration = random.nextInt(8) + 4;
        currentShakeIntensity = (random.nextFloat() * 0.4f + 0.6f) * (float) Math.min(getDeltaMovement().length() * 0.5, 1);
        shakePhase = random.nextFloat() * (float) Math.PI * 2;
    }

    private void resetShake() {
        shakeCooldown = random.nextInt(9) + 2;
        shakeDuration = 0;
        currentShakeIntensity = 0;
    }

    private void applyShake() {
        float bumpX = (float) Math.sin(shakePhase) * currentShakeIntensity * 2f;
        float bumpY = (float) Math.cos(shakePhase * 1.5f) * currentShakeIntensity * 1.5f;
        
        float newPitch = this.getXRot() + bumpY * 0.5f;
        float newRoll = this.getRoll() + bumpX * 0.5f;
        
        this.setXRot(newPitch);
        this.setRoll(newRoll);
    }
}