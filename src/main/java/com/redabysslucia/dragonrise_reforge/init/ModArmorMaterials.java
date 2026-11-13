package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

/**
 * 自定义护甲材质：整体对齐下界合金套，只有头盔额外 +4 护甲值。
 *
 * <table>
 *   <tr><th></th><th>护甲</th><th>耐久</th></tr>
 *   <tr><td>头盔</td><td>7（下界合金 3 + 4）</td><td>407（11×37）</td></tr>
 *   <tr><td>胸甲</td><td>8</td><td>592（16×37）</td></tr>
 *   <tr><td>护腿</td><td>6</td><td>555（15×37）</td></tr>
 *   <tr><td>靴子</td><td>3</td><td>481（13×37）</td></tr>
 * </table>
 *
 * 韧性 3.0、击退抗性 0.1、附魔能力 15、修复材料下界合金锭 —— 与 {@code ArmorMaterials.NETHERITE} 一致。
 */
public final class ModArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> REGISTRY =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Dragonrise_reforge.MODID);

    /** 下界合金的耐久系数（原版 37） */
    public static final int NETHERITE_DURABILITY_FACTOR = 37;

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> NETHERITE_HEAVY = REGISTRY.register(
            "netherite_heavy",
            () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET, 7,
                            ArmorItem.Type.CHESTPLATE, 8,
                            ArmorItem.Type.LEGGINGS, 6,
                            ArmorItem.Type.BOOTS, 3,
                            ArmorItem.Type.BODY, 8),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(Items.NETHERITE_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("netherite"))),
                    3.0F,
                    0.1F));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    private ModArmorMaterials() {
    }
}
