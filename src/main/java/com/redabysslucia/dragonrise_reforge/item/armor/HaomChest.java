package com.redabysslucia.dragonrise_reforge.item.armor;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.init.ModAttributes;
import com.redabysslucia.dragonrise_reforge.init.ModArmorMaterials;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import com.redabysslucia.dragonrise_reforge.client.renderer.armor.SbmArmorItemRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.armor.SbmArmorRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Haom 护甲（作战服）。SBM 模型：{@code models/bedrock/armor/haom.geo.json}。
 *
 * <p>重装组 A（HAOM / ADE3 / SAWKM-1）：韧性 5.0、击退抗性 1.0、护甲 11、子弹抗性满耐久 0.5（随损伤线性衰减）、附魔能力 15、耐久 ×37；耐久 = Type.CHESTPLATE.getDurability(37)。
 */
public class HaomChest extends ArmorItem {
    /** SBM armor model: assets/dragonrise_reforge/models/bedrock/armor/haom.geo.json */
    public static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "models/bedrock/armor/haom.geo.json");
    /** SBM armor texture: assets/dragonrise_reforge/textures/armor/haom.png */
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/armor/haom.png");

    public HaomChest() {
        super(ModArmorMaterials.HEAVY_A, Type.CHESTPLATE,
                new Properties().durability(Type.CHESTPLATE.getDurability(37)));
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
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers base = super.getDefaultAttributeModifiers(stack);
        double factor = 0.5 * Math.max(0, 1 - (double) stack.getDamageValue() / stack.getMaxDamage());
        return base.withModifierAdded(
                ModAttributes.BULLET_RESISTANCE,
                new AttributeModifier(Mod.ATTRIBUTE_MODIFIER, factor, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(this.getEquipmentSlot()));
    }

}
