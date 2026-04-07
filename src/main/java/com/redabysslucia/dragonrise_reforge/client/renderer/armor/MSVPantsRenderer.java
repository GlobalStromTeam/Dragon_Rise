package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.MSVPants;
import com.redabysslucia.dragonrise_reforge.client.model.armor.MSVPantsModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MSVPantsRenderer extends GeoArmorRenderer<MSVPants> {
	public MSVPantsRenderer() {
		super(new MSVPantsModel());
		this.body = new GeoBone(null, "armorBody", false, (double) 0, false, false);
		this.rightArm = new GeoBone(null, "armorRightArm", false, (double) 0, false, false);
		this.leftArm = new GeoBone(null, "armorLeftArm", false, (double) 0, false, false);
		this.head = new GeoBone(null, "armorHead", false, (double) 0, false, false);
		this.rightLeg = new GeoBone(null, "armorRightLeg", false, (double) 0, false, false);
		this.leftLeg = new GeoBone(null, "armorLeftLeg", false, (double) 0, false, false);
	}

	@Override
	public RenderType getRenderType(MSVPants animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}