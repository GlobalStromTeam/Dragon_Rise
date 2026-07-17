package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.redabysslucia.dragonrise_reforge.entities.utils.VehicleLeashData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VehicleLeashRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        record LeashEntry(Vec3 from, Vec3 to, boolean isChain) {}
        List<LeashEntry> entries = new ArrayList<>();
        Map<UUID, VehicleEntity> vehicles = new HashMap<>();

        Iterable<Entity> entities = mc.level.entitiesForRendering();
        if (entities == null) return;
        for (Entity entity : entities) {
            if (entity instanceof VehicleEntity vehicle) {
                vehicles.put(vehicle.getUUID(), vehicle);
            }
        }

        for (VehicleEntity vehicle : vehicles.values()) {
            Optional<UUID> holderUUID = vehicle.getEntityData().get(VehicleLeashData.LEASH_HOLDER);
            if (holderUUID.isEmpty()) continue;
            Entity holder = vehicles.get(holderUUID.get());
            if (holder == null) continue;

            boolean isChain = vehicle.getEntityData().get(VehicleLeashData.CHAIN_LEASHED);
            entries.add(new LeashEntry(
                    new Vec3(vehicle.getX(), vehicle.getY() + vehicle.getBbHeight() * 0.5, vehicle.getZ()),
                    new Vec3(holder.getX(), holder.getY() + holder.getBbHeight() * 0.5, holder.getZ()),
                    isChain));
        }

        if (entries.isEmpty()) return;

        var poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        RenderSystem.setShaderTexture(0, VehicleLeashData.LEAD_TEXTURE);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (LeashEntry entry : entries) {
            if (!entry.isChain) {
                drawTexturedLine(builder, poseStack, entry.from, entry.to, false);
            }
        }
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.setShaderTexture(0, VehicleLeashData.CHAIN_TEXTURE);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (LeashEntry entry : entries) {
            if (entry.isChain) {
                drawTexturedLine(builder, poseStack, entry.from, entry.to, true);
            }
        }
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    /** 原版风渲染，rotate 对铁链把贴图旋转 90 度 */
    private static void drawTexturedLine(BufferBuilder builder, PoseStack poseStack, Vec3 from, Vec3 to, boolean rotate) {
        Vec3 dir = to.subtract(from);
        double length = dir.length();
        if (length < 0.01) return;

        Matrix4f matrix = poseStack.last().pose();
        Vec3 forward = dir.normalize();
        Vec3 look = new Vec3(Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector());
        Vec3 side = forward.cross(look).normalize().scale(0.05);

        float texScale = (float) (length * 2);
        Vector3f s = side.toVector3f();
        float x1 = (float) from.x, y1 = (float) from.y, z1 = (float) from.z;
        float x2 = (float) to.x, y2 = (float) to.y, z2 = (float) to.z;

        // 铁链旋转 90 度：U 沿宽度方向，V 沿长度方向
        float u1 = 0, u2 = rotate ? 1 : texScale;
        float v1 = 0, v2 = rotate ? texScale : 1;

        vertex(builder, matrix, x1 - s.x(), y1 - s.y(), z1 - s.z(), u1, v1);
        vertex(builder, matrix, x1 + s.x(), y1 + s.y(), z1 + s.z(), u2, v1);
        vertex(builder, matrix, x2 + s.x(), y2 + s.y(), z2 + s.z(), u2, v2);
        vertex(builder, matrix, x2 - s.x(), y2 - s.y(), z2 - s.z(), u1, v2);
    }

    private static void vertex(BufferBuilder builder, Matrix4f m, float x, float y, float z, float u, float v) {
        builder.vertex(m, x, y, z).uv(u, v).color(255, 255, 255, 255).endVertex();
    }
}
