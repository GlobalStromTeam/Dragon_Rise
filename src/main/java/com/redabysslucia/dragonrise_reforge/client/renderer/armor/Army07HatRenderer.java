package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.redabysslucia.dragonrise_reforge.client.model.armor.Army07HatModel;
import com.redabysslucia.dragonrise_reforge.client.model.armor.Ocean07HelmetModel;
import com.redabysslucia.dragonrise_reforge.item.armor.Army07Hat;
import com.redabysslucia.dragonrise_reforge.item.armor.Ocean07Helmet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class Army07HatRenderer extends GeoArmorRenderer<Army07Hat> {
	public Army07HatRenderer() {
		super(new Army07HatModel());
		this.head = new GeoBone(null, "armorHead", false, (double) 0, false, false);
		this.body = new GeoBone(null, "armorBody", false, (double) 0, false, false);
		this.rightArm = new GeoBone(null, "armorRightArm", false, (double) 0, false, false);
		this.leftArm = new GeoBone(null, "armorLeftArm", false, (double) 0, false, false);
	}

	@Override
	public RenderType getRenderType(Army07Hat animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}