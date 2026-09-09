package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.recipe.SignalFlareDyeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 信号棒特殊配方序列化器：染料染色（CustomRecipe，保留 NBT）。
 */
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Dragonrise_reforge.MODID);

    public static final RegistryObject<RecipeSerializer<SignalFlareDyeRecipe>> SIGNAL_FLARE_DYE =
            REGISTRY.register("signal_flare_dye", SignalFlareDyeRecipe.Serializer::new);
}
