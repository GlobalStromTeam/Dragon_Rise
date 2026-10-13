package com.redabysslucia.dragonrise_reforge.recipe;

import com.atsuishio.superbwarfare.item.IDyeableSmokeItem;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import com.redabysslucia.dragonrise_reforge.init.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 信号棒 + 任意染料 = 染成对应颜色的信号棒（保留 SmokeSeconds 等其它 NBT）。
 *
 * <p>1.21.1 迁移：
 * <ul>
 *   <li>{@code CraftingContainer} → {@link CraftingInput}</li>
 *   <li>{@code CustomRecipe} 增加 {@link CraftingBookCategory} 构造参数</li>
 *   <li>{@code assemble(..., RegistryAccess)} → {@code assemble(CraftingInput, HolderLookup.Provider)}</li>
 *   <li>{@link SimpleCraftingRecipeSerializer} 改为按 category 的工厂</li>
 * </ul>
 */
public class SignalFlareDyeRecipe extends CustomRecipe {

    public SignalFlareDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    private record Found(ItemStack flare, ItemStack dye) {
    }

    private static Found find(CraftingInput inv) {
        ItemStack flare = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (s.getItem() == ModItems.SIGNAL_FLARE.get()) {
                if (!flare.isEmpty()) return new Found(ItemStack.EMPTY, ItemStack.EMPTY);
                flare = s;
            } else if (s.getItem() instanceof DyeItem) {
                if (!dye.isEmpty()) return new Found(ItemStack.EMPTY, ItemStack.EMPTY);
                dye = s;
            } else {
                return new Found(ItemStack.EMPTY, ItemStack.EMPTY);
            }
        }
        return new Found(flare, dye);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        Found f = find(inv);
        return !f.flare().isEmpty() && !f.dye().isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        Found f = find(inv);
        if (f.flare().isEmpty() || f.dye().isEmpty()) return ItemStack.EMPTY;
        int color = ((DyeItem) f.dye().getItem()).getDyeColor().getMapColor().col;
        ItemStack result = f.flare().copy();
        result.setCount(1);
        if (result.getItem() instanceof IDyeableSmokeItem dyeable) {
            dyeable.setColor(result, color);
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SIGNAL_FLARE_DYE.get();
    }

    /** 1.21.1：SimpleCraftingRecipeSerializer 以 category 工厂构造。 */
    public static class Serializer extends SimpleCraftingRecipeSerializer<SignalFlareDyeRecipe> {
        public Serializer() {
            super(SignalFlareDyeRecipe::new);
        }
    }
}
