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
                        //中国
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ99A.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ99BH.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.VT4A1.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.VT4B.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTQ15.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ59A.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.TYPE100.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZBL08.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CM34.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CSK181.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZLT_11.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZBD04A.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AA625E.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.SX1.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Z9.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Z20.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Z10A.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Z10ME.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Q5.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J8.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J10.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J10C.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.JF17.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J11.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J15T.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J16.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J20.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J35.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J20VTOL.get()));

                        //俄罗斯
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T90MH.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T80.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T80B.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMP3.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMPT72.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.PROJECT640.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.S2S38.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.KA50.get()));

                        //北约
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M1A2SEPV2.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AMX56.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.STRV103.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M3A3.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CV90.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.FAVA.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.F14.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AV8B.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CAMEL.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.UH60.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AH64.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.EC665.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.JAS39E.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.REFALE.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.REFALEAA.get()));

                                output.accept(ContainerBlockItem.createInstance(ModEntities.WLHGZU23.get()));
                                output.accept(ContainerBlockItem.createInstance(ModEntities.WLSC.get()));
                                output.accept(ContainerBlockItem.createInstance(ModEntities.motuo.get()));
                                output.accept(ContainerBlockItem.createInstance(ModEntities.TOYOTASEIKI.get()));
                                output.accept(ContainerBlockItem.createInstance(ModEntities.CYBORG_TANK.get()));
                                output.accept(ContainerBlockItem.createInstance(ModEntities.HYR0.get()));



                    }
                    ).build()
    );

//    public static final RegistryObject<CreativeModeTab> WW2_TAB = TABS.register("ww2_tab",
//            () -> CreativeModeTab.builder()
//                    .title(Component.translatable("item_group.dragonrise_reforge.ww2title"))
//                    .icon(() -> new ItemStack(ModItems.TAB_ICON.get()))
//                    .displayItems((param, output) -> {
//                        //ww2
//                        output.accept(ContainerBlockItem.createInstance(ModEntities.PANZER4.get()));
//                        output.accept(ContainerBlockItem.createInstance(ModEntities.T3476.get()));
//                        output.accept(ContainerBlockItem.createInstance(ModEntities.IS2.get()));
//                        output.accept(ContainerBlockItem.createInstance(ModEntities.TIGER.get()));
//                        output.accept(ContainerBlockItem.createInstance(ModEntities.CAMEL.get()));
//                            }
//                    ).build()
//    );

    public static final RegistryObject<CreativeModeTab> MISC_TAB = TABS.register("misc_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.dragonrise_reforge.misc_tab.title"))
                    .icon(() -> new ItemStack(ModItems.MISC_ICON.get()))
                    .displayItems((param, output) -> {
                        output.accept(ModItems.MK19_DEPLOYER.get());
                        output.accept(ModItems.ZU23_DEPLOYER.get());
                        output.accept(ModItems.qjz89_DEPLOYER.get());
                        output.accept(ModItems.DSHK_DEPLOYER.get());
                        output.accept(ModItems.M2_DEPLOYER.get());
                        output.accept(ModItems.SHIELD_DEPLOYER.get());
                        output.accept(ModItems.CNCHEST.get());
                        output.accept(ModItems.CNJUSTCHEST.get());
                        output.accept(ModItems.CN21.get());
                        output.accept(ModItems.CNFAST.get());
                    }
                    ).build()
    );
}