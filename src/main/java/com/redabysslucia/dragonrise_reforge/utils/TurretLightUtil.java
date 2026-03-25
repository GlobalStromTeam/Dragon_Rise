package com.redabysslucia.dragonrise_reforge.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import com.atsuishio.superbwarfare.entity.OBBEntity;
import com.atsuishio.superbwarfare.tools.OBB;
import com.atsuishio.superbwarfare.tools.OBB.Part;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TurretLightUtil {

    // 存储实体的光源方块位置和放置时间
    private static final Map<Entity, LightInfo> lightInfoMap = new HashMap<>();

    /**
     * 处理坦克主武器开火时的炮口火光效果
     * @param entity 目标实体
     * @param level 游戏世界
     * @param shootTimer 开火动画计时器
     */
    public static void handleTurretFireLight(Entity entity, Level level, int shootTimer) {
        if (!level.isClientSide() && entity instanceof OBBEntity obbEntity) {
            // 检查是否正在开火
            if (shootTimer > 0) {
                // 查找Turret obb
                for (OBB obb : obbEntity.getOBBs()) {
                    if (obb.part() == Part.TURRET) {
                        // 获取Turret obb的中心位置
                        Vec3 obbCenter = OBB.vector3dToVec3(obb.center());
                        // 获取OBB的旋转
                        org.joml.Quaterniond rotation = obb.rotation();

                        // 计算OBB的北方方向（基于OBB的旋转）
                        // 初始北方方向是(0, 0, 1)
                        org.joml.Vector3d northDirection = new org.joml.Vector3d(0, 0, 1);
                        // 应用OBB的旋转
                        northDirection.rotate(rotation);
                        
                        // 计算偏移后的位置：往OBB的北方偏移两格，Y轴下降一格
                        double offsetX = northDirection.x * 4;
                        double offsetY = 0;
                        double offsetZ = northDirection.z * 4;
                        
                        // 计算最终位置
                        double finalX = obbCenter.x + offsetX;
                        double finalY = obbCenter.y + offsetY;
                        double finalZ = obbCenter.z + offsetZ;
                        
                        // 转换为方块位置
                        BlockPos pos = new BlockPos((int) Math.floor(finalX), (int) Math.floor(finalY), (int) Math.floor(finalZ));

                        // 放置光源方块（红石火把，亮度更高）
                        if (level.isEmptyBlock(pos)) {
                            level.setBlock(pos, Blocks.LIGHT.defaultBlockState(), 3);
                            // 记录光源信息
                            lightInfoMap.put(entity, new LightInfo(pos, level.getGameTime()));
                        }
                        break;
                    }
                }
            } else {
                // 删除之前的光源方块
                LightInfo lightInfo = lightInfoMap.get(entity);
                if (lightInfo != null) {
                    level.removeBlock(lightInfo.pos, false);
                    lightInfoMap.remove(entity);
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
}