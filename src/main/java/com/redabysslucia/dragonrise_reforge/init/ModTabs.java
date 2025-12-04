package com.redabysslucia.dragonrise_reforge.init;

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

        public static final RegistryObject<CreativeModeTab> MBT_TAB = TABS.register("dragonrise_reforge",
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("item_group.dragonrise_reforge.title"))
                        .icon(() -> new ItemStack(ModItems.TAB_ICON.get()))
                        .displayItems((param, output) -> {
                            output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ99A.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ99BH.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.ZBL08.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.ZLT_11.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.ZBD04A.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.AA625E.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.Z10ME.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.J10.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.T80.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.KA50.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.M1A2SEPV2.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.AMX56.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.SX1.get()));
                            output.accept(ContainerBlockItem.createInstance(ModEntities.STRV103.get()));
                                }
                        ).build()
        );
}