package com.redabyss.ralmod.RalModClient.Model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redabyss.ralmod.RalMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class gl6Model extends EntityModel<Entity> {
        public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(RalMod.MODID, "models/block/gl6"), "main");
    private final ModelPart bottom;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart bracket;
    private final ModelPart connect;

    public gl6Model(ModelPart root) {
        this.bottom = root.getChild("bottom");
        this.left = root.getChild("left");
        this.right = root.getChild("right");
        this.bracket = root.getChild("bracket");
        this.connect = this.bracket.getChild("connect");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bottom = partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left = partdefinition.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 17).addBox(-0.4F, 8.9333F, -4.6F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-0.9F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = left.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(1.6F, 11.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition right = partdefinition.addOrReplaceChild("right", CubeListBuilder.create().texOffs(16, 13).addBox(-3.7F, 8.9333F, -4.6F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-0.9F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r2 = right.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 9).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-1.7F, 11.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bracket = partdefinition.addOrReplaceChild("bracket", CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -3.3333F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(12, 19).addBox(3.0F, -1.3333F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 19).addBox(-4.0F, -1.3333F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.3333F, 0.0F));

        PartDefinition connect = bracket.addOrReplaceChild("connect", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 11.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F))
                .texOffs(20, 4).addBox(2.25F, 11.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F))
                .texOffs(0, 23).addBox(-3.25F, 11.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, -9.3333F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
    @Override
    public void setupAnim(Entity p_102618_, float p_102619_, float p_102620_, float p_102621_, float p_102622_, float p_102623_) {

    }
    @Override
    public void renderToBuffer(PoseStack p_103111_, VertexConsumer p_103112_, int p_103113_, int p_103114_, float p_103115_, float p_103116_, float p_103117_, float p_103118_) {

    }
}
