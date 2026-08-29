package com.redabysslucia.dragonrise_reforge.datagen;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    private static final TagKey<Item> DYES = commonItemTag("dyes");

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    private static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, path);
    }

    private static TagKey<Item> commonItemTag(String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static String getItemName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        buildMiscRecipes(writer);
        buildArmorRecipes(writer);
    }

    private static void buildMiscRecipes(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPRAY_CAN.get())
                .pattern("III")
                .pattern("IDI")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('D', DYES)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Tags.Items.INGOTS_IRON))
                .save(writer, loc(getItemName(ModItems.SPRAY_CAN.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.KEVLAR.get())
                .pattern("LLL")
                .pattern("LHL")
                .pattern("LLL")
                .define('L', Items.LEATHER)
                .define('H', Items.HONEYCOMB)
                .unlockedBy(getHasName(Items.LEATHER), has(Items.LEATHER))
                .save(writer, loc(getItemName(ModItems.KEVLAR.get())));
    }

    private static void buildArmorRecipes(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MSV_CHEST.get())
                .pattern("KDK")
                .pattern("K K")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('D', Tags.Items.DYES_YELLOW)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.MSV_CHEST.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GORKA3.get())
                .pattern("KFK")
                .pattern("KKK")
                .pattern("KKK")
                .define('K', ModItems.KEVLAR.get())
                .define('F', Tags.Items.INGOTS_IRON)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.GORKA3.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GORKA3_LEGGINGS.get())
                .pattern("KFK")
                .pattern("K K")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('F', Tags.Items.INGOTS_IRON)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.GORKA3_LEGGINGS.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ALJIN_HELMET.get())
                .pattern("KKK")
                .pattern("KFK")
                .define('K', ModItems.KEVLAR.get())
                .define('F', Tags.Items.INGOTS_IRON)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.ALJIN_HELMET.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MED21_CHEST.get())
                .pattern("KRK")
                .pattern("KDK")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('R', Tags.Items.DYES_RED)
                .define('D', Tags.Items.DYES_GREEN)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.MED21_CHEST.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.T21_HELMET.get())
                .pattern("KGK")
                .pattern("KDK")
                .define('K', ModItems.KEVLAR.get())
                .define('G', Tags.Items.DYES_GRAY)
                .define('D', Tags.Items.DYES_GREEN)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.T21_HELMET.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FAST_HELMET.get())
                .pattern("KKK")
                .pattern("KDK")
                .define('K', ModItems.KEVLAR.get())
                .define('D', Tags.Items.DYES_YELLOW)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.FAST_HELMET.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SNIPER21_HELMET.get())
                .pattern("LOL")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('L', Items.OAK_LEAVES)
                .define('O', Items.OAK_LOG)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.SNIPER21_HELMET.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PANTS21.get())
                .pattern("KGK")
                .pattern("KDK")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('G', Tags.Items.DYES_GRAY)
                .define('D', Tags.Items.DYES_GREEN)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.PANTS21.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MSV_PANTS.get())
                .pattern("KDK")
                .pattern("KKK")
                .pattern("KKK")
                .define('K', ModItems.KEVLAR.get())
                .define('D', Tags.Items.DYES_YELLOW)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.MSV_PANTS.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DESERT07_HELMET.get())
                .pattern("KSK")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('S', Items.SAND)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.DESERT07_HELMET.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DESERT07_CHEST.get())
                .pattern("KSK")
                .pattern("K K")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('S', Items.SAND)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.DESERT07_CHEST.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DESERT07_PANTS.get())
                .pattern("KSK")
                .pattern("K K")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('S', Items.SAND)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.DESERT07_PANTS.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OCEAN07_HELMET.get())
                .pattern("KBK")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('B', Tags.Items.DYES_BLUE)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.OCEAN07_HELMET.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OCEAN07_CHEST.get())
                .pattern("KBK")
                .pattern("K K")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('B', Tags.Items.DYES_BLUE)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.OCEAN07_CHEST.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OCEAN07_PANTS.get())
                .pattern("KBK")
                .pattern("K K")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('B', Tags.Items.DYES_BLUE)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.OCEAN07_PANTS.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.UN_HELMET.get())
                .pattern("KLK")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('L', Tags.Items.DYES_LIGHT_BLUE)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.UN_HELMET.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.KR06_HELMET.get())
                .pattern("KNK")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('N', Tags.Items.DYES_BLACK)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.KR06_HELMET.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.KR06_CHEST.get())
                .pattern("KNK")
                .pattern("K K")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('N', Tags.Items.DYES_BLACK)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.KR06_CHEST.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.KR06_PANTS.get())
                .pattern("KNK")
                .pattern("K K")
                .pattern("K K")
                .define('K', ModItems.KEVLAR.get())
                .define('N', Tags.Items.DYES_BLACK)
                .unlockedBy(getHasName(ModItems.KEVLAR.get()), has(ModItems.KEVLAR.get()))
                .save(writer, loc(getItemName(ModItems.KR06_PANTS.get())));
    }
}
