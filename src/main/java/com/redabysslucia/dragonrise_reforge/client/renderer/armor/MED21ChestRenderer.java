package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.MED21Chest;
import com.redabysslucia.dragonrise_reforge.client.model.armor.MED21ChestModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MED21ChestRenderer extends GeoArmorRenderer<MED21Chest> {
	public MED21ChestRenderer() {
		super(new MED21ChestModel());
		this.body = new GeoBone(null, "armorBody", false, (double) 0, false, false);
		this.rightArm = new GeoBone(null, "armorRightArm", false, (double) 0, false, false);
		this.leftArm = new GeoBone(null, "armorLeftArm", false, (double) 0, false, false);
		this.head = new GeoBone(null, "armorHead", false, (double) 0, false, false);
	}

	@Override
	public RenderType getRenderType(MED21Chest animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}