package com.redabysslucia.dragonrise_reforge.utils;

import com.mojang.logging.LogUtils;
import com.redabysslucia.dragonrise_reforge.client.sound.SupersonicSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SpeedSoundUtil {

    private static final Logger LOGGER = LogUtils.getLogger();

    // 存储实体的持续生成计时器
    private static final Map<Entity, Integer> entityTimers = new HashMap<>();

    // 存储实体的持续播放音量
    private static final Map<Entity, Float> entityVolumes = new HashMap<>();

    // 存储实体的声音实例
    private static final Map<Entity, SupersonicSoundInstance> soundInstances = new HashMap<>();

    // 存储实体是否正在播放
    private static final Map<Entity, Boolean> isPlaying = new HashMap<>();

    // 音爆粒子和单次音效的速度阈值（方块/tick）
    private static final double SONIC_BOOM_THRESHOLD = 3.4;

    // 持续音效的速度阈值（方块/tick）
    private static final double LOOP_SOUND_THRESHOLD = 1.5;

    // 粒子持续时间（tick）- 0.8秒 = 16 tick
    private static final int PARTICLE_DURATION = 16;

    // 粒子生成间隔（tick）
    private static final int PARTICLE_INTERVAL = 2;

    // 音效范围（方块）
    private static final double SOUND_RANGE = 150.0;

    // 是否启用调试模式
    private static boolean debugMode = true;

    /**
     * 检查实体速度并播放音效
     * @param entity 要检查的实体
     * @param boomSound 音爆单次音效
     * @param loopSound 持续播放的音效
     */
    public static void checkSpeedAndPlaySound(Entity entity, SoundEvent boomSound, SoundEvent loopSound) {

        Level level = entity.level();

        // 只在客户端进行速度检测和音效播放
        if (!level.isClientSide()) {
            return;
        }

        // 获取实体类型名称
        String entityTypeName = getEntityTypeName(entity);

        // 检查实体的速度
        double currentSpeed = getCurrentSpeed(entity);
        boolean hasSonicBoomSpeed = currentSpeed >= SONIC_BOOM_THRESHOLD;
        boolean hasLoopSoundSpeed = currentSpeed >= LOOP_SOUND_THRESHOLD;

        // 获取计时器
        int timer = entityTimers.getOrDefault(entity, 0);

        // 处理音爆音效和粒子
        if (hasSonicBoomSpeed) {
            // 播放音效（只在首次触发时播放）
            if (timer == 0) {
                playSoundToNearbyPlayers(entity, boomSound);

                if (debugMode) {
                    LOGGER.info("[SpeedSoundUtil] Playing sonic boom for entity: {}, Speed: {:.3f}", entityTypeName, currentSpeed);
                }
            }

            // 检查是否到了粒子生成时间
            if (timer < PARTICLE_DURATION && timer % PARTICLE_INTERVAL == 0) {
                // 生成音爆云粒子效果
                spawnSonarCloudParticles(entity);
            }

            // 增加计时器
            entityTimers.put(entity, timer + 1);
        } else {
            // 速度低于阈值时重置计时器
            entityTimers.put(entity, 0);
        }

        // 处理持续音效
        if (hasLoopSoundSpeed) {
            // 播放持续音效（跟随实体）
            playContinuousSound(entity, loopSound);
        } else {
            // 停止持续音效
            stopContinuousSound(entity);
        }

        // 清理不再需要的状态
        if (!entity.isAlive()) {
            entityTimers.remove(entity);
            entityVolumes.remove(entity);
            stopContinuousSound(entity);
        }

        // 清理已停止的声音实例
        cleanupStoppedSounds();
    }

    /**
     * 播放跟随实体的持续音效
     */
    private static void playContinuousSound(Entity entity, SoundEvent soundEvent) {
        Boolean wasPlaying = isPlaying.getOrDefault(entity, false);
        SupersonicSoundInstance instance = soundInstances.get(entity);

        if (!wasPlaying) {
            // 之前没播放，创建新实例
            instance = new SupersonicSoundInstance(soundEvent, Minecraft.getInstance(), entity);
            instance.setShouldPlay(true);
            Minecraft.getInstance().getSoundManager().play(instance);
            soundInstances.put(entity, instance);
            isPlaying.put(entity, true);
        } else if (instance != null) {
            // 已经在播放了，更新 shouldPlay
            instance.setShouldPlay(true);
        }
    }

    /**
     * 停止持续音效
     */
    private static void stopContinuousSound(Entity entity) {
        SupersonicSoundInstance instance = soundInstances.get(entity);
        if (instance != null) {
            instance.setShouldPlay(false);
        }
        isPlaying.put(entity, false);
    }

    /**
     * 清理已停止的声音实例
     */
    private static void cleanupStoppedSounds() {
        Iterator<Map.Entry<Entity, SupersonicSoundInstance>> iterator = soundInstances.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Entity, SupersonicSoundInstance> entry = iterator.next();
            if (entry.getKey().isRemoved()) {
                iterator.remove();
                isPlaying.remove(entry.getKey());
            }
        }
    }

    /**
     * 生成音爆云粒子效果
     */
    private static void spawnSonarCloudParticles(Entity entity) {
        Level level = entity.level();
        RandomSource random = level.getRandom();
        Vec3 entityPos = entity.position();

        // 获取实体尺寸并再缩小一半
        double width = entity.getBbWidth();
        double height = entity.getBbHeight() * 0.3;

        // 获取实体朝向
        Vec3 lookAngle = entity.getLookAngle().normalize();

        // 往前偏移1格
        Vec3 forwardOffset = lookAngle.scale(1);
        Vec3 centerPos = entityPos.add(forwardOffset);

        // 计算右侧向量
        Vec3 right = new Vec3(-lookAngle.z, 0, lookAngle.x).normalize();

        // 生成竖着的圆柱/锥形粒子 - 增加数量
        int particleCount = 20;
        for (int i = 0; i < particleCount; i++) {
            // 竖着的圆柱形分布：上下延伸，向四周扩散
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * width;
            double heightOffset = (random.nextDouble() - 0.5) * height * 3;

            // 计算粒子位置 - 竖着的圆柱，位置往前偏移
            double x = centerPos.x + Math.cos(angle) * radius;
            double y = centerPos.y + heightOffset;
            double z = centerPos.z + Math.sin(angle) * radius;

            // 粒子速度 - 减少一半
            double speedX = Math.cos(angle) * 0.15;
            double speedY = (random.nextDouble() - 0.5) * 0.25;
            double speedZ = Math.sin(angle) * 0.15;

            // 生成 flash 粒子
            level.addParticle(ParticleTypes.FLASH, x, y, z, speedX, speedY, speedZ);
        }

        // 在实体上方生成更多粒子，形成蘑菇云效果 - 增加数量
        int topCount = 20;
        for (int i = 0; i < topCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * width * 0.8;
            double yOffset = height * 1.5 + random.nextDouble() * height;

            double x = centerPos.x + Math.cos(angle) * radius;
            double y = centerPos.y + yOffset;
            double z = centerPos.z + Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.FLASH, x, y, z, 0, 0.1, 0);
        }

        // 在实体下方也生成一些粒子 - 增加数量
        int bottomCount = 20;
        for (int i = 0; i < bottomCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * width * 0.5;
            double yOffset = -height * 0.5 - random.nextDouble() * height;

            double x = centerPos.x + Math.cos(angle) * radius;
            double y = centerPos.y + yOffset;
            double z = centerPos.z + Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.FLASH, x, y, z, 0, -0.05, 0);
        }
    }

    /**
     * 获取实体类型名称
     */
    private static String getEntityTypeName(Entity entity) {
        String className = entity.getClass().getSimpleName();
        // 移除末尾的 "Entity" 后缀
        if (className.endsWith("Entity")) {
            return className.substring(0, className.length() - 6);
        }
        return className;
    }

    /**
     * 获取实体当前速度
     */
    private static double getCurrentSpeed(Entity entity) {
        Vec3 deltaMovement = entity.getDeltaMovement();
        return deltaMovement.horizontalDistance();
    }

    /**
     * 向周围玩家播放音效
     */
    private static void playSoundToNearbyPlayers(Entity entity, SoundEvent soundEvent) {
        Level level = entity.level();
        AABB boundingBox = entity.getBoundingBox().inflate(SOUND_RANGE);

        // 查找周围的玩家
        for (Player player : level.getEntitiesOfClass(Player.class, boundingBox)) {
            // 计算距离
            double distance = entity.distanceTo(player);
            if (distance <= SOUND_RANGE) {
                // 播放音效，音量根据距离调整
                float volume = 6.0f - (float)(distance / SOUND_RANGE);
                player.playSound(soundEvent, volume, 6.0f);
            }
        }
    }
}
