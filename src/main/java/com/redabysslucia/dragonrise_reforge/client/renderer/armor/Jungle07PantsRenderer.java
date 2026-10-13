package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Jungle07Pants;
import com.redabysslucia.dragonrise_reforge.client.model.armor.Jungle07PantsModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class Jungle07PantsRenderer extends GeoArmorRenderer<Jungle07Pants> {
	public Jungle07PantsRenderer() {
		super(new Jungle07PantsModel());
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
		// 闈村瓙涓庤￥瀛愬悎浣擄細鎶よ吙妲戒綅鏃朵篃鏄剧ず闈村瓙楠ㄩ
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
	public RenderType getRenderType(Jungle07Pants animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}
