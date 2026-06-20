package com.redabysslucia.dragonrise_reforge.client.outline.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Predicate;
import net.neoforged.fml.ModList;

public class OutlineRenderer {
    
    // 确保类被注册到事件总线上
    public static void register() {
        // 直接注册到事件总线
        NeoForge.EVENT_BUS.register(OutlineRenderer.class);
        System.out.println("OutlineRenderer registered to event bus");
    }

    private static final boolean OCULUS_LOADED = ModList.get().isLoaded("oculus");
    private static final boolean AR_LOADED = ModList.get().isLoaded("acceleratedrendering");

    private static OutlineFramebuffer maskFramebuffer;
    private static OutlineFramebuffer edgeFramebuffer;
    private static OutlineFramebuffer blurFramebuffer1;
    private static OutlineFramebuffer blurFramebuffer2;
    private static OutlineFramebuffer blackOutlineFramebuffer;

    private static OutlineShader maskShader;
    private static OutlineShader blurShader;
    private static OutlineShader applyShader;
    private static OutlineShader blackOutlineShader;

    private static int processedMaskTexture = 0;

    private static int quadVAO = -1;
    private static int quadVBO = -1;

    private static BufferBuilder maskBuffer = null;
    private static MultiBufferSource.BufferSource maskBufferSource = null;
    // TODO: Replaced ByteBufferBuilder with BufferBuilder for 1.21.1 API
    // BufferBuilder constructor now takes (ByteBufferBuilder, Mode, VertexFormat)
    private static java.nio.ByteBuffer byteBuffer = null;

    private static int lastEntityCount = 0;
    private static int framesSinceLastCheck = 0;
    private static final int CHECK_INTERVAL = 5;

    private static boolean renderThisFrame = false;

    public enum RenderMode {
        OFF,
        OUTLINE,
        OVERLAY
    }

    private static RenderMode renderMode = RenderMode.OFF;

    public static void setRenderMode(RenderMode mode) {
        renderMode = mode;
    }

    public static RenderMode getRenderMode() {
        return renderMode;
    }

    private static Predicate<Entity> outlinePredicate = entity -> entity instanceof LivingEntity;
    private static Function<Entity, Integer> colorProvider = null;

    private static float outlineR = 1.0f;
    private static float outlineG = 1.0f;
    private static float outlineB = 1.0f;
    private static float outlineA = 1.0f;

    private static boolean useColoredOutline = true;
    private static boolean useBlackOutline = true;

    public static void setOutlineColorProvider(Function<Entity, Integer> provider) { colorProvider = provider; }
    public static void setOutlinePredicate(Predicate<Entity> predicate) { outlinePredicate = predicate; }
    public static void setOutlineColor(float r, float g, float b, float a) { outlineR = r; outlineG = g; outlineB = b; outlineA = a; }
    public static void setUseColoredOutline(boolean use) { useColoredOutline = use; }
    public static boolean isUsingColoredOutline() { return useColoredOutline; }
    public static void setUseBlackOutline(boolean use) { useBlackOutline = use; }
    public static boolean isUsingBlackOutline() { return useBlackOutline; }

    public static void init() {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        maskFramebuffer = new OutlineFramebuffer(width, height, true);
        edgeFramebuffer = new OutlineFramebuffer(width, height);
        blurFramebuffer1 = new OutlineFramebuffer(width, height);
        blurFramebuffer2 = new OutlineFramebuffer(width, height);
        blackOutlineFramebuffer = new OutlineFramebuffer(width, height);

        try {
            maskShader = new OutlineShader("mask");
            blurShader = new OutlineShader("blur");
            applyShader = new OutlineShader("apply");
            blackOutlineShader = new OutlineShader("black_outline");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load outline shaders", e);
        }

        createQuadVAO();
        // TODO: Reimplement for 1.21.1 BufferBuilder API
        // BufferBuilder constructor changed: new BufferBuilder(ByteBufferBuilder, Mode, VertexFormat)
        // MultiBufferSource.immediate() now takes ByteBufferBuilder instead of BufferBuilder
        byteBuffer = java.nio.ByteBuffer.allocateDirect(262144);
        maskBuffer = null;
        maskBufferSource = null;
    }

    private static void minimalStateReset() {
        GL20.glUseProgram(0);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        for (int i = 0; i < 4; i++) {
            RenderSystem.activeTexture(GL13.GL_TEXTURE0 + i);
            GlStateManager._bindTexture(0);
        }
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GL30.glBindVertexArray(0);
    }

    private static void createQuadVAO() {
        quadVAO = GL30.glGenVertexArrays();
        quadVBO = GL30.glGenBuffers();
        float[] quadVertices = {
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };
        GL30.glBindVertexArray(quadVAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, quadVBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, quadVertices, GL30.GL_STATIC_DRAW);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL30.glEnableVertexAttribArray(1);
        GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        GL30.glBindVertexArray(0);
    }

    public static void resize(int width, int height) {
        if (maskFramebuffer != null) {
            maskFramebuffer.resize(width, height);
            edgeFramebuffer.resize(width, height);
            blurFramebuffer1.resize(width, height);
            blurFramebuffer2.resize(width, height);
            blackOutlineFramebuffer.resize(width, height);
        }
    }

    public static void cleanup() {
        if (maskFramebuffer != null) {
            maskFramebuffer.destroy();
            edgeFramebuffer.destroy();
            blurFramebuffer1.destroy();
            blurFramebuffer2.destroy();
            blackOutlineFramebuffer.destroy();
            maskFramebuffer = null;
        }
        if (maskShader != null) {
            maskShader.close();
            blurShader.close();
            applyShader.close();
            blackOutlineShader.close();
            maskShader = null;
        }
        if (quadVAO != -1) {
            GL30.glDeleteVertexArrays(quadVAO);
            GL30.glDeleteBuffers(quadVBO);
            quadVAO = -1;
            quadVBO = -1;
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (renderMode == RenderMode.OFF) return;

        if (OCULUS_LOADED) {
            // 简化处理，跳过阴影渲染
            return;
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {

            Minecraft mc = Minecraft.getInstance();
            boolean arDisabled = false;

            try {
                MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                bufferSource.endBatch();

                if (AR_LOADED) {
                    // 简化处理，跳过AR加速禁用
                    arDisabled = false;
                }

                if (captureMobMasks(event.getPoseStack(), event.getProjectionMatrix())) {
                    renderThisFrame = true;
                } else if (renderThisFrame) {
                    renderThisFrame = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (arDisabled && AR_LOADED) {
                    // 简化处理，跳过AR加速重置
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderScreenEffects(RenderGuiEvent.Pre event) {
        if (renderThisFrame) {
            drawOverlayToScreen();
            renderThisFrame = false;
        }
    }

    private static boolean captureMobMasks(PoseStack poseStack, Matrix4f projectionMatrix) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || maskFramebuffer == null) {
            System.out.println("captureMobMasks: level or framebuffer is null");
            return false;
        }

        int count = 0;
        framesSinceLastCheck++;
        if (framesSinceLastCheck >= CHECK_INTERVAL) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (outlinePredicate.test(entity)) {
                    count++;
                    // 检查是否是 geo 模型实体
                    net.minecraft.client.renderer.entity.EntityRenderer<? super Entity> renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
                    if (renderer != null) {
                        System.out.println("captureMobMasks: found entity " + entity.getName() + " with renderer " + renderer.getClass().getName());
                    }
                }
            }
            lastEntityCount = count;
            framesSinceLastCheck = 0;
            System.out.println("captureMobMasks: found " + count + " entities to outline");
        } else {
            count = lastEntityCount;
        }

        if (count == 0) {
            System.out.println("captureMobMasks: no entities to outline");
            return false;
        }
        if (!useColoredOutline && !useBlackOutline) {
            System.out.println("captureMobMasks: outline disabled");
            return false;
        }

        try {
            System.out.println("captureMobMasks: rendering entity masks");
            renderEntityMasks(poseStack, projectionMatrix);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    private static void drawOverlayToScreen() {
        try {
            RenderSystem.clearStencil(0);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

            Minecraft mc = Minecraft.getInstance();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            bufferSource.endBatch();
            mc.getMainRenderTarget().bindWrite(false);

            extractEdges();
            applyBlur(true);
            applyBlur(false);

            if (useBlackOutline) {
                createBlackOutline();
            }

            compositeOutline();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            minimalStateReset();
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    }

    private static void renderEntityMasks(PoseStack poseStack, Matrix4f projectionMatrix) {
        Minecraft mc = Minecraft.getInstance();
        int targetWidth = mc.getMainRenderTarget().width;
        int targetHeight = mc.getMainRenderTarget().height;

        if (maskFramebuffer.width != targetWidth || maskFramebuffer.height != targetHeight) {
            resize(targetWidth, targetHeight);
        }

        int mainFbo = mc.getMainRenderTarget().frameBufferId;
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainFbo);

        int depthType = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_READ_FRAMEBUFFER,
                GL30.GL_DEPTH_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
        int depthId = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_READ_FRAMEBUFFER,
                GL30.GL_DEPTH_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);

        boolean canShareDepth = (depthId != 0);

        if (canShareDepth) {
            maskFramebuffer.attachExternalDepth(depthType, depthId);
        } else {
            maskFramebuffer.restoreInternalDepth();
        }

        maskFramebuffer.bind();

        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        if (canShareDepth) {
            RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT, false);
        } else {
            RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, false);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL30.GL_LEQUAL);

        RenderSystem.depthMask(false);
        RenderSystem.colorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_STENCIL_TEST);

        if (maskBufferSource == null) {
            // TODO: Reimplement for 1.21.1 - BufferBuilder constructor changed
            maskBuffer = null;
            maskBufferSource = null;
        }

        boolean oldRenderShadows = mc.options.entityShadows().get();
        boolean oldRenderHitboxes = mc.getEntityRenderDispatcher().shouldRenderHitBoxes();

        mc.getEntityRenderDispatcher().setRenderShadow(false);
        mc.getEntityRenderDispatcher().setRenderHitBoxes(false);

        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(true);

        int renderDistanceChunks = mc.options.renderDistance().get();
        double renderDistanceBlocks = renderDistanceChunks * 16.0;

        FogType fogType = mc.gameRenderer.getMainCamera().getFluidInCamera();
        if (fogType == FogType.WATER || fogType == FogType.LAVA) {
            renderDistanceBlocks = Math.min(renderDistanceBlocks, 32.0);
        }

        double renderDistanceSquared = renderDistanceBlocks * renderDistanceBlocks;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!outlinePredicate.test(entity)) continue;
            if (entity == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON) continue;

            double lerpX = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
            double lerpY = entity.yOld + (entity.getY() - entity.yOld) * partialTick;
            double lerpZ = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;

            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            double dx = lerpX - cameraPos.x;
            double dy = lerpY - cameraPos.y;
            double dz = lerpZ - cameraPos.z;
            double distanceSquared = dx * dx + dy * dy + dz * dz;

            if (distanceSquared > renderDistanceSquared) continue;

            if (colorProvider != null) {
                int color = colorProvider.apply(entity);
                float r = ((color >> 16) & 0xFF) / 255.0f;
                float g = ((color >> 8) & 0xFF) / 255.0f;
                float b = (color & 0xFF) / 255.0f;
                float a = ((color >> 24) & 0xFF) / 255.0f;
                if (a == 0.0f) a = 1.0f;

                RenderSystem.setShaderColor(r, g, b, a);
            } else {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }

            EntityMaskRenderer.renderEntityMask(entity, lerpX, lerpY, lerpZ, partialTick, poseStack, projectionMatrix, maskBufferSource);

            if (colorProvider != null) {
                maskBufferSource.endBatch();
            }
        }

        if (colorProvider == null) {
            maskBufferSource.endBatch();
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        mc.getEntityRenderDispatcher().setRenderShadow(oldRenderShadows);
        mc.getEntityRenderDispatcher().setRenderHitBoxes(oldRenderHitboxes);

        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL30.GL_LEQUAL);
        maskFramebuffer.restoreInternalDepth();
        mc.getMainRenderTarget().bindWrite(false);
    }

    private static void extractEdges() {
        edgeFramebuffer.bind();
        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT, false);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        GL30.glBindVertexArray(quadVAO);

        maskShader.use();
        int sourceMaskTex = (processedMaskTexture != 0) ? processedMaskTexture : maskFramebuffer.getColorTexture();
        maskShader.setTexture("DiffuseSampler", sourceMaskTex);
        maskShader.setUniform("InSize", (float) maskFramebuffer.width, (float) maskFramebuffer.height);
        maskShader.setUniform("OutSize", (float) edgeFramebuffer.width, (float) edgeFramebuffer.height);

        drawFullscreenQuad();

        blurFramebuffer1.bind();
        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT, false);

        applyShader.use();
        applyShader.setTexture("DiffuseSampler", edgeFramebuffer.getColorTexture());
        applyShader.setUniform("OutlineColor", 1.0f, 1.0f, 1.0f, 1.0f);
        applyShader.setUniform("UseSourceColor", 1);

        drawFullscreenQuad();

        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    private static void applyBlur(boolean horizontal) {
        OutlineFramebuffer source = horizontal ? blurFramebuffer1 : blurFramebuffer2;
        OutlineFramebuffer target = horizontal ? blurFramebuffer2 : blurFramebuffer1;

        target.bind();
        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT, false);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();

        GL30.glBindVertexArray(quadVAO);

        blurShader.use();
        blurShader.setTexture("DiffuseSampler", source.getColorTexture());
        blurShader.setUniform("InSize", (float) source.width, (float) source.height);
        blurShader.setUniform("OutSize", (float) target.width, (float) target.height);
        blurShader.setUniform("BlurDir", horizontal ? 1.0f : 0.0f, horizontal ? 0.0f : 1.0f);
        blurShader.setUniform("Radius", 1.0f);

        drawFullscreenQuad();

        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    private static void createBlackOutline() {
        blackOutlineFramebuffer.bind();
        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT, false);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();

        GL30.glBindVertexArray(quadVAO);

        blackOutlineShader.use();
        blackOutlineShader.setTexture("DiffuseSampler", edgeFramebuffer.getColorTexture());
        blackOutlineShader.setTexture("EntityMask", maskFramebuffer.getColorTexture());
        blackOutlineShader.setUniform("InSize", (float) edgeFramebuffer.width, (float) edgeFramebuffer.height);
        blackOutlineShader.setUniform("OutSize", (float) blackOutlineFramebuffer.width, (float) blackOutlineFramebuffer.height);
        blackOutlineShader.setUniform("Radius", 1.5f);

        drawFullscreenQuad();

        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    private static void compositeOutline() {
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        GL30.glBindVertexArray(quadVAO);

        int sourceMaskTex = (processedMaskTexture != 0) ? processedMaskTexture : maskFramebuffer.getColorTexture();

        if (renderMode == RenderMode.OUTLINE) {
            if (useBlackOutline) {
                applyShader.use();
                applyShader.setTexture("DiffuseSampler", blackOutlineFramebuffer.getColorTexture());
                applyShader.setUniform("OutlineColor", 0.0f, 0.0f, 0.0f, 1.0f);
                applyShader.setUniform("UseSourceColor", 0);
                drawFullscreenQuad();
            }

            if (useColoredOutline) {
                applyShader.use();
                applyShader.setTexture("DiffuseSampler", blurFramebuffer1.getColorTexture());
                applyShader.setUniform("OutlineColor", outlineR, outlineG, outlineB, outlineA);

                if (colorProvider != null) {
                    applyShader.setUniform("UseSourceColor", 1);
                } else {
                    applyShader.setUniform("UseSourceColor", 0);
                }

                drawFullscreenQuad();
            }
        } else if (renderMode == RenderMode.OVERLAY) {
            applyShader.use();
            applyShader.setTexture("DiffuseSampler", sourceMaskTex);
            applyShader.setUniform("OutlineColor", outlineR, outlineG, outlineB, outlineA);

            if (colorProvider != null) {
                applyShader.setUniform("UseSourceColor", 1);
            } else {
                applyShader.setUniform("UseSourceColor", 0);
            }
            drawFullscreenQuad();
        }

        processedMaskTexture = 0;
        RenderSystem.disableBlend();
    }

    private static void drawFullscreenQuad() {
        GL30.glBindVertexArray(quadVAO);
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
    }
}