package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.redabysslucia.dragonrise_reforge.client.model.armor.CN21Model;
import com.redabysslucia.dragonrise_reforge.client.model.armor.CNfastModel;
import com.redabysslucia.dragonrise_reforge.item.armor.CN21;
import com.redabysslucia.dragonrise_reforge.item.armor.CNfast;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class CN21Renderer extends GeoArmorRenderer<CN21> {
	public CN21Renderer() {
		super(new CN21Model());
		this.body = new GeoBone(null, "armorBody", false, (double) 0, false, false);
		this.rightArm = new GeoBone(null, "armorRightArm", false, (double) 0, false, false);
		this.leftArm = new GeoBone(null, "armorLeftArm", false, (double) 0, false, false);
	}

	@Override
	public RenderType getRenderType(CN21 animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}
