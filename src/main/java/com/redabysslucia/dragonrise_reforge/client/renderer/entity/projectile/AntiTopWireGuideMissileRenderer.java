package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.BedrockModelRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redabysslucia.dragonrise_reforge.entities.projectile.AntiTopWireGuideMissileEntity;
import com.redabysslucia.dragonrise_reforge.resource.model.ProjectileModelReloadListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class AntiTopWireGuideMissileRenderer extends EntityRenderer<AntiTopWireGuideMissileEntity> {

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "models/bedrock/projectile/anti_top_wire_guide_missile.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/bedrock/projectile/anti_top_wire_guide_missile.png");
    private static final ResourceLocation FLARE_TEXTURE = ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/bedrock/projectile/flare.png");

    public AntiTopWireGuideMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AntiTopWireGuideMissileEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(AntiTopWireGuideMissileEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        if (entity.tickCount <= entity.getHiddenTicks()) return;

        var model = ProjectileModelReloadListener.INSTANCE.getModel(MODEL);
        if (model == null) return;

        poseStack.pushPose();

        poseStack.translate(0f, entity.getBbHeight() / 2, 0f);

        var lookAngle = entity.getLookAngle();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) VehicleVecUtils.getYRotFromVector(lookAngle)));
        poseStack.mulPose(Axis.XP.rotationDegrees(-(float) VehicleVecUtils.getXRotFromVector(lookAngle) + 180f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));

        var flare = model.getBone("flare");
        boolean hasFlare = flare != null;
        if (hasFlare) {
            flare.visible = false;
        }

        model.renderToBuffer(
                poseStack,
                buffer,
                RenderType.entityCutout(TEXTURE),
                BedrockModelRenderTypes.polyMeshCutout(TEXTURE),
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        var emissive = entity.getEmissiveTexture();
        if (emissive != null) {
            model.renderToBuffer(
                    poseStack,
                    buffer,
                    RenderType.entityCutout(emissive),
                    BedrockModelRenderTypes.polyMeshCutout(emissive),
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );
        }

        if (hasFlare && entity.tickCount > entity.getFlareHiddenTicks()) {
            flare.visible = true;
            flare.rotation.rotationZ(2.5f * (float) (Math.random() - 0.5));
            flare.xScale = (float) ((2 * Math.random() - 1) * 0.4 + 1.6);
            flare.yScale = (float) ((2 * Math.random() - 1) * 0.4 + 1.6);
            flare.zScale = (float) ((2 * Math.random() - 1) * 0.4 + 1.6);
            flare.render(
                    poseStack,
                    buffer.getBuffer(RenderType.eyes(FLARE_TEXTURE)),
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );
        }

        poseStack.popPose();
    }
}
