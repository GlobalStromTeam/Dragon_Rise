package com.redabysslucia.dragonrise_reforge.utils;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class GeoBasedParticleUtil {

    // 存储不同模型中骨骼的枢轴点数据
    private static final Map<String, Map<String, Vec3>> modelBonePivots = new HashMap<>();

    /**
     * 根据手动指定的坐标生成粒子效果
     *
     * @param entity 实体
     * @param localX 局部X坐标（模型空间）
     * @param localY 局部Y坐标（模型空间）
     * @param localZ 局部Z坐标（模型空间）
     */
    public static void spawnParticlesFromManualPosition(Entity entity, double localX, double localY, double localZ) {
        if (!entity.level().isClientSide()) return;

        // 创建局部坐标向量
        Vec3 pivot = new Vec3(localX, localY, localZ);

        // 计算世界坐标中的位置
        Vec3 worldPos = calculateBoneWorldPosition(entity, pivot);

        // 生成粒子效果
        spawnParticlesAtPosition(entity, worldPos);
    }

    /**
     * 计算骨骼在世界坐标中的位置
     * @param entity 实体
     * @param pivot 骨骼枢轴点（模型空间）
     * @return 骨骼在世界坐标中的位置
     */
    private static Vec3 calculateBoneWorldPosition(Entity entity, Vec3 pivot) {
        // 获取实体位置
        Vec3 entityPos = entity.position();

        // 获取实体旋转角度（以弧度为单位）
        double yaw = entity.getYRot() * Math.PI / 180.0;
        double pitch = entity.getXRot() * Math.PI / 180.0;

        // 应用旋转到枢轴点
        // 首先应用Yaw旋转
        double rotatedX = pivot.x() * Math.cos(yaw) - (-pivot.z()) * Math.sin(yaw);
        double rotatedZ = pivot.x() * Math.sin(yaw) + (-pivot.z()) * Math.cos(yaw);
        double rotatedY = pivot.y();

        // 然后应用Pitch旋转（简化处理，只旋转X和Y轴）
        double finalX = rotatedX * Math.cos(pitch) - rotatedY * Math.sin(pitch);
        double finalY = rotatedX * Math.sin(pitch) + rotatedY * Math.cos(pitch);
        double finalZ = rotatedZ;

        // 转换为世界坐标（注意：模型空间和世界空间的单位可能不同，这里假设1:1）
        // 调整坐标，使模型原点与实体原点对齐
        double scale = 1.0 / 16.0; // Minecraft模型单位通常是1/16方块

        Vec3 result = new Vec3(
                entityPos.x() + finalX * scale,
                entityPos.y() + finalY * scale,
                entityPos.z() + finalZ * scale
        );
        
        System.out.println("Calculated bone position: " + result);
        return result;
    }

    /**
     * 在指定位置生成粒子效果
     * @param entity 实体
     * @param position 粒子生成位置
     */
    private static void spawnParticlesAtPosition(Entity entity, Vec3 position) {
        System.out.println("Spawning particles at position: " + position);
        // 生成烟雾粒子
        for (int i = 0; i < 3; i++) {
            // 随机偏移，使粒子效果更自然
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.2;
            double offsetZ = (Math.random() - 0.5) * 0.5;

            // 粒子速度
            double velocityX = (Math.random() - 0.3) * 0.1;
            double velocityY = Math.random() * 0.015 + 0.02;
            double velocityZ = (Math.random() - 0.3) * 0.1;

            // 生成粒子
            entity.level().addParticle(
                    ParticleTypes.SMOKE,
                    position.x() + offsetX,
                    position.y() + offsetY,
                    position.z() + offsetZ,
                    velocityX,
                    velocityY,
                    velocityZ
            );
        }
    }
}