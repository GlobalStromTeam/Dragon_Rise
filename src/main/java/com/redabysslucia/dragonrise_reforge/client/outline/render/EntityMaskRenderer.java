package com.redabysslucia.dragonrise_reforge.client.outline.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class EntityMaskRenderer {

    /**
     * Renders an entity to a separate buffer source for masking
     * This buffer source is independent of the main rendering pipeline
     */
    public static void renderEntityMask(Entity entity, double lerpX, double lerpY, double lerpZ,
                                        float partialTick, PoseStack poseStack, Matrix4f projectionMatrix,
                                        MultiBufferSource.BufferSource maskBufferSource) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        // 获取实体的渲染器
        EntityRenderer<? super Entity> renderer = dispatcher.getRenderer(entity);
        
        // 检查是否是有效的渲染器
        if (renderer != null) {
            System.out.println("Rendering entity mask for " + entity.getName() + ", renderer: " + renderer.getClass().getName());
            
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

            double x = lerpX - cameraPos.x;
            double y = lerpY - cameraPos.y;
            double z = lerpZ - cameraPos.z;

            poseStack.pushPose();
            poseStack.translate(x, y, z);

            try {
                float lerpYaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTick;

                // 直接调用渲染器的 render 方法，传入 maskBufferSource
                // 这样可以处理所有类型的实体，包括 geo 模型实体
                // 使用正确的参数列表：Entity, float, float, PoseStack, MultiBufferSource, int
                System.out.println("Calling renderer.render for " + entity.getName());
                renderer.render(
                        entity,
                        lerpYaw,
                        partialTick,
                        poseStack,
                        maskBufferSource,
                        15728880
                );
                System.out.println("Successfully rendered entity mask for " + entity.getName());

            } catch (Exception e) {
                // 跳过渲染失败的实体
                System.out.println("Error rendering entity mask for " + entity.getName() + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                poseStack.popPose();
            }
        } else {
            System.out.println("No renderer found for entity: " + entity.getName());
        }
    }
}