package com.redabysslucia.dragonrise_reforge.recipe;

import com.atsuishio.superbwarfare.item.IDyeableSmokeItem;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import com.redabysslucia.dragonrise_reforge.init.ModRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 信号棒 + 任意染料 = 染成对应颜色的信号棒（保留 SmokeSeconds 等其它 NBT）。
 */
public class SignalFlareDyeRecipe extends CustomRecipe {

    public SignalFlareDyeRecipe(net.minecraft.resources.ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    private record Found(ItemStack flare, ItemStack dye) {
    }

    private static Found find(CraftingContainer inv) {
        ItemStack flare = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
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
    public boolean matches(CraftingContainer inv, Level level) {
        Found f = find(inv);
        return !f.flare().isEmpty() && !f.dye().isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
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

    public static class Serializer extends net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<SignalFlareDyeRecipe> {
        public Serializer() {
            super(SignalFlareDyeRecipe::new);
        }
    }
}
