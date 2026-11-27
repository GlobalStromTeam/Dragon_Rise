package com.redabysslucia.dragonrise_reforge.item.armor;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.init.ModAttributes;
import com.atsuishio.superbwarfare.tiers.ModArmorMaterial;
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
 * BritishSoldier 护甲（作战服）。SBM 模型：{@code models/bedrock/armor/british_soldier_chest.geo.json}。
 *
 * <p>Superb Warfare 胶结硬质合金（老护甲数值）：护甲 3/8/6、韧性 4.0、击退抗性 0.05、附魔能力 10、耐久 ×50；耐久 = Type.CHESTPLATE.getDurability(50)。
 */
public class BritishSoldierChest extends ArmorItem {
    /** SBM armor model: assets/dragonrise_reforge/models/bedrock/armor/british_soldier_chest.geo.json */
    public static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "models/bedrock/armor/british_soldier_chest.geo.json");
    /** SBM armor texture: assets/dragonrise_reforge/textures/armor/british_soldier.png */
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/armor/british_soldier.png");

    public BritishSoldierChest() {
        super(ModArmorMaterial.CEMENTED_CARBIDE, Type.CHESTPLATE,
                new Properties().durability(Type.CHESTPLATE.getDurability(50)));
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
