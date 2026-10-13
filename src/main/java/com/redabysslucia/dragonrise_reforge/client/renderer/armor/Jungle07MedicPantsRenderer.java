package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Jungle07MedicPants;
import com.redabysslucia.dragonrise_reforge.client.model.armor.Jungle07MedicPantsModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class Jungle07MedicPantsRenderer extends GeoArmorRenderer<Jungle07MedicPants> {
	public Jungle07MedicPantsRenderer() {
		super(new Jungle07MedicPantsModel());
		this.body = new GeoBone(null, "armorBody", false, (double) 0, false, false);
		this.rightArm = new GeoBone(null, "armorRightArm", false, (double) 0, false, false);
		this.leftArm = new GeoBone(null, "armorLeftArm", false, (double) 0, false, false);
		this.head = new GeoBone(null, "armorHead", false, (double) 0, false, false);
		this.rightLeg = new GeoBone(null, "armorRightLeg", false, (double) 0, false, false);
		this.leftLeg = new GeoBone(null, "armorLeftLeg", false, (double) 0, false, false);
		this.rightBoot = new GeoBone(null, "armorRightBoot", false, (double) 0, false, false);
		this.leftBoot = new GeoBone(null, "armorLeftBoot", false, (double) 0, false, false);
	}

	@Override
	protected void applyBoneVisibilityBySlot(EquipmentSlot slot) {
		super.applyBoneVisibilityBySlot(slot);
		// 靴子与裤子合体：护腿槽位时也显示靴子骨骼
		if (slot == EquipmentSlot.LEGS) {
			if (this.rightBoot != null) {
				this.setBoneVisible(this.rightBoot, true);
			}
			if (this.leftBoot != null) {
				this.setBoneVisible(this.leftBoot, true);
			}
		}
	}

	@Override
	public RenderType getRenderType(Jungle07MedicPants animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}
