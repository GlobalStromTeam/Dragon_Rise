package com.redabysslucia.dragonrise_reforge.client.renderer.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Gorka3Leggings;
import com.redabysslucia.dragonrise_reforge.client.model.armor.Gorka3LeggingsModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class Gorka3LeggingsRenderer extends GeoArmorRenderer<Gorka3Leggings> {
	public Gorka3LeggingsRenderer() {
		super(new Gorka3LeggingsModel());
		// 保留身体和腿部的骨骼，设置为可见
		this.body = new GeoBone(null, "armorBody", true, (double) 0, false, false);
		this.rightLeg = new GeoBone(null, "armorRightLeg", true, (double) 0, false, false);
		this.leftLeg = new GeoBone(null, "armorLeftLeg", true, (double) 0, false, false);
	}

	@Override
	public RenderType getRenderType(Gorka3Leggings animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}