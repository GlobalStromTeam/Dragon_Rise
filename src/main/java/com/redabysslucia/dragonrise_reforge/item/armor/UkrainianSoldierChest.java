package com.redabysslucia.dragonrise_reforge.item.armor;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.init.ModAttributes;
import com.atsuishio.superbwarfare.tiers.ModArmorMaterial;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.UUID;
import java.util.function.Consumer;
import com.redabysslucia.dragonrise_reforge.client.renderer.armor.SbmArmorItemRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.armor.SbmArmorRenderer;
import net.minecraft.resources.ResourceLocation;

public class UkrainianSoldierChest extends ArmorItem {
    /** SBM armor model: assets/dragonrise_reforge/models/bedrock/armor/ukrainian_soldier_chest.geo.json */
    public static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "models/bedrock/armor/ukrainian_soldier_chest.geo.json");
    /** SBM armor texture: assets/dragonrise_reforge/textures/armor/ukrainian_soldier.png */
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/armor/ukrainian_soldier.png");

    public UkrainianSoldierChest() {
        super(ModArmorMaterial.CEMENTED_CARBIDE, Type.CHESTPLATE, new Properties());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                SbmArmorRenderer renderer = SbmArmorRenderer.get(GEO, equipmentSlot, TEXTURE);
                if (renderer == null) {
                    return original;
                }
                renderer.preparePose(livingEntity, itemStack, equipmentSlot, original);
                return renderer;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return SbmArmorItemRenderer.get(GEO, TEXTURE);
            }
        });
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = super.getDefaultAttributeModifiers(slot);
        UUID uuid = new UUID(slot.toString().hashCode(), 0);
        if (slot == EquipmentSlot.CHEST) {
            map = HashMultimap.create(map);
            map.put(ModAttributes.BULLET_RESISTANCE.get(), new AttributeModifier(uuid, Mod.ATTRIBUTE_MODIFIER,
                    0.5 * Math.max(0, 1 - (double) stack.getDamageValue() / stack.getMaxDamage()), AttributeModifier.Operation.ADDITION));
        }
        return map;
    }

}
