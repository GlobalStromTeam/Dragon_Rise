package com.redabysslucia.dragonrise_reforge.init;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.common.container.ContainerBlockItem;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class ModTabs {
        public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Dragonrise_reforge.MODID);

        public static final RegistryObject<CreativeModeTab> BLOCK_TAB = TABS.register("dragonrise_reforge",
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("item_group.dragonrise_reforge.title"))
                        .icon(() -> new ItemStack(ModItems.CONTAINER.get()))
                        .displayItems((param, output) -> {
                            output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ99A.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.KA50.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.T80.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.J10.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.AA625E.get()));
                                }
                        ).build()
        );
}