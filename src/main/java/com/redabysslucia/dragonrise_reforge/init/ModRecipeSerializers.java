package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.recipe.SignalFlareDyeRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 信号棒特殊配方序列化器：染料染色（CustomRecipe，保留 NBT）。
 *
 * <p>1.21.1 里配方序列化器挂在原版 {@link BuiltInRegistries#RECIPE_SERIALIZER} 下
 * （Forge 时代的 {@code ForgeRegistries.RECIPE_SERIALIZERS} 已不存在）。
 */
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Dragonrise_reforge.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SignalFlareDyeRecipe>> SIGNAL_FLARE_DYE =
            REGISTRY.register("signal_flare_dye", SignalFlareDyeRecipe.Serializer::new);
}
