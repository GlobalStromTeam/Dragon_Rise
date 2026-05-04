package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.UNHelmet;
import com.redabysslucia.dragonrise_reforge.client.model.armor.UNHelmetModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class UNHelmetRenderer extends GeoArmorRenderer<UNHelmet> {
	public UNHelmetRenderer() {
		super(new UNHelmetModel());
		this.head = new GeoBone(null, "armorHead", false, (double) 0, false, false);
		this.body = new GeoBone(null, "armorBody", false, (double) 0, false, false);
		this.rightArm = new GeoBone(null, "armorRightArm", false, (double) 0, false, false);
		this.leftArm = new GeoBone(null, "armorLeftArm", false, (double) 0, false, false);
	}

	@Override
	public RenderType getRenderType(UNHelmet animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}
