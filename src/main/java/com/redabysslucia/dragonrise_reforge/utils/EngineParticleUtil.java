package com.redabysslucia.dragonrise_reforge.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import com.atsuishio.superbwarfare.entity.OBBEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import com.atsuishio.superbwarfare.tools.OBB.Part;

import java.util.HashMap;
import java.util.Map;

public class EngineParticleUtil {

    // 存储实体的MainEngine obb框位置，用于检测移动
    private static final Map<Entity, Vec3> lastEnginePositions = new HashMap<>();

    /**
     * 在实体的MainEngine obb框位置生成粒子效果
     * @param entity 目标实体
     * @param level 游戏世界
     */
    public static void spawnMainEngineParticles(Entity entity, Level level) {
        if (level.isClientSide() && entity instanceof OBBEntity obbEntity) {
            // 获取实体的所有obb框
            for (OBB obb : obbEntity.getOBBs()) {
                // 检查是否是MainEngine
                if (obb.part == Part.EMPTY) {  // 修复：使用Part.MAIN_ENGINE
                    // 获取obb框的中心位置
                    Vec3 obbCenter = OBB.vector3dToVec3(obb.center);

                    // 获取上一次的位置
                    Vec3 lastPosition = lastEnginePositions.get(entity);

                    // 如果是第一次或者位置有变化，生成粒子效果
                    if (lastPosition == null || lastPosition.distanceToSqr(obbCenter) > 0.01) {  // 修复：使用distanceToSqr
                        // 生成烟雾粒子
                        spawnSmokeParticlesAtOBB(level, obb);

                        // 更新位置
                        lastEnginePositions.put(entity, obbCenter);
                    }
                }
            }
        }
    }


    /**
     * 在OBB框位置生成烟雾粒子
     * @param level 游戏世界
     * @param obb OBB框
     */
    private static void spawnSmokeParticlesAtOBB(Level level, OBB obb) {
        // 获取OBB框的中心位置
        Vec3 center = OBB.vector3dToVec3(obb.center);

        // 生成烟雾粒子
        for (int i = 0; i < 1; i++) {
            // 随机偏移，范围在OBB框内
            double offsetX = (Math.random() - 0.5) * (obb.extents().x * 2);
            double offsetY = (Math.random() - 0.5) * (obb.extents().y * 2);
            double offsetZ = (Math.random() - 0.5) * (obb.extents().z * 2);

            // 随机速度
            double motionX = (Math.random() - 0.5) * 0.1;
            double motionY = Math.random() * 0.02 + 0.01;
            double motionZ = (Math.random() - 0.5) * 0.1;

            level.addParticle(
                    ParticleTypes.POOF,
                    center.x + offsetX,
                    center.y + offsetY,
                    center.z + offsetZ,
                    motionX,
                    motionY,
                    motionZ
            );
        }
    }
}