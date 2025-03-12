package com.redabyss.ralmod.RalModRegist;

import com.redabyss.ralmod.RalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
public class CreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, RalMod.MODID
    );
    public static final RegistryObject<CreativeModeTab>  MainTab = CREATIVE_MODE_TAB.register(
            "maintab",
            ()-> CreativeModeTab.builder()
            .title(Component.translatable(RalMod.MODID))
            .icon(()->new ItemStack(Items.gl6item.get()))
            .displayItems((parm,output) ->
                    output.accept(Items.gl6item.get()))
            .build()
    );
    public static void register(IEventBus eventBus) {CREATIVE_MODE_TAB.register(eventBus);}
}
