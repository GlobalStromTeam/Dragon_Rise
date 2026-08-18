package com.redabysslucia.dragonrise_reforge.init;

import com.atsuishio.superbwarfare.item.container.ContainerBlockItem;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
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
                        //中国 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ99A.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ99AH.get()));
//                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ99BH.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTQ15.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.VT4A1.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.VT4B.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ59A.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.TYPE100.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTD05.get()));
                        //中国 装甲车辆
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZBD04A.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZBL08.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZLT_11.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CSK181.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CM34.get()));
                        //中国 防空
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AA625E.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.SX1.get()));
                        //中国 直升机
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Z10A.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Z10ME.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Z20.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Z9.get()));
                        //中国 固定翼
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J20.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J20VTOL.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J35.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J16.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J15T.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J11.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J10C.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J10.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.JF17.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.J8.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.Q5.get()));

                        //俄罗斯 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T90MH.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T80.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T80B.get()));
                        //俄罗斯 装甲车辆
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMPT72.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMP3.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BMD4M.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.R2S25M.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.PROJECT640.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AKM.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.BRDM2.get()));
                        //俄罗斯 防空
                        output.accept(ContainerBlockItem.createInstance(ModEntities.S2S38.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.TUNGUSKA.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZSU234.get()));
                        //俄罗斯 直升机
                        output.accept(ContainerBlockItem.createInstance(ModEntities.KA50.get()));

                        //美国 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M1A2SEPV2.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M1A1HC.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M10BOOKER.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M270.get()));
                        //美国 装甲车辆
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M3A3.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M113.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.HUMVEE.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.HUMVEETOW.get()));
                        //美国 轻型车辆
                        output.accept(ContainerBlockItem.createInstance(ModEntities.FAVA.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.HUMVEETOW.get()));
                        //美国 直升机
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AH64.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AH1F.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.UH60.get()));
                        //美国 固定翼
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AC130.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.F14.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.F16C.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AV8B.get()));

                        //法国 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AMX56.get()));
                        //法国 直升机
                        output.accept(ContainerBlockItem.createInstance(ModEntities.EC665.get()));
                        //法国 固定翼
                        output.accept(ContainerBlockItem.createInstance(ModEntities.REFALE.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.REFALEAA.get()));

                        //德国 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.L1A2.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.LEOPARD2A4.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.FLARAKPZ1.get()));

                        //瑞典 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.STRV103.get()));
                        //瑞典 装甲车辆
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CV90.get()));
                        //瑞典 固定翼
                        output.accept(ContainerBlockItem.createInstance(ModEntities.JAS39E.get()));

                        //欧洲 直升机
                        output.accept(ContainerBlockItem.createInstance(ModEntities.NH90.get()));

                        //虚构/特殊
                        output.accept(ContainerBlockItem.createInstance(ModEntities.TJGC.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.HYR0.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CYBORG_TANK.get()));
                        //中国 民用/娱乐
                        output.accept(ContainerBlockItem.createInstance(ModEntities.WLHGZU23.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.WLSC.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.PZBJY.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.TOYOTASEIKI.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.motuo.get()));
                        //自动炮塔
                        output.accept(ContainerBlockItem.createInstance(ModEntities.NPDS114.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.NPDS514.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.NPDS810.get()));
//                      output.accept(ContainerBlockItem.createInstance(ModEntities.SD905.get()));
                    }
                    ).build()
    );

    public static final RegistryObject<CreativeModeTab> WW2_TAB = TABS.register("ww2_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.dragonrise_reforge.ww2title"))
                    .icon(() -> new ItemStack(ModItems.WW2_TAB_ICON.get()))
                    .displayItems((param, output) -> {
                        //苏联 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.IS2.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T3485.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.KV1.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T3476.get()));

                        //德国 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.MAUS.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.TIGER.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.PANZER4.get()));

                        //美国 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.PERSHING.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M4A2105.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M4A2.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.M3Stuart.get()));
                        //美国 装甲车辆
                        output.accept(ContainerBlockItem.createInstance(ModEntities.LVT.get()));
                        //美国 固定翼
                        output.accept(ContainerBlockItem.createInstance(ModEntities.F4U.get()));

                        //英国 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.COMET.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CHURCHILL_VII.get()));
                        //英国 固定翼
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CAMEL.get()));

                        //日本 坦克
                        output.accept(ContainerBlockItem.createInstance(ModEntities.type97Q.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.type97.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.type3.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.DARKBEAR.get()));

                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZBD05.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.MARKV.get()));

                        output.accept(ContainerBlockItem.createInstance(ModEntities.AAV7A1.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.AAVC7C1.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.T72B3.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.ZTZ96A.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.MV3.get()));
                        output.accept(ContainerBlockItem.createInstance(ModEntities.CHALLENGER_DS.get()));
                    }
                    ).build()
    );

    public static final RegistryObject<CreativeModeTab> MISC_TAB = TABS.register("misc_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.dragonrise_reforge.misc_tab.title"))
                    .icon(() -> new ItemStack(ModItems.MISC_ICON.get()))
                    .displayItems((param, output) -> {
                        output.accept(ModItems.MK19_DEPLOYER.get());
                        output.accept(ModItems.ZU23_DEPLOYER.get());
                        output.accept(ModItems.HJ8_DEPLOYER.get());
                        output.accept(ModItems.R9M133_DEPLOYER.get());
                        output.accept(ModItems.qjz89_DEPLOYER.get());
                        output.accept(ModItems.DSHK_DEPLOYER.get());
                        output.accept(ModItems.M2_DEPLOYER.get());
                        output.accept(ModItems.SHIELD_DEPLOYER.get());
                        output.accept(ModItems.AMMO_SUPPLY_STATION_DEPLOYER.get());
                        output.accept(DragonVehicleDeployer.VEHICLE_DEPLOYER_BLOCK_ITEM.get());
                        output.accept(ModItems.CNCHEST.get());
                        output.accept(ModItems.CN21.get());
                        output.accept(ModItems.CNFAST.get());
                        output.accept(ModItems.MSV_CHEST.get());
                        output.accept(ModItems.ALJIN_HELMET.get());
                        output.accept(ModItems.GORKA3.get());
                        output.accept(ModItems.GORKA3_LEGGINGS.get());
                        output.accept(ModItems.MED21_CHEST.get());
                        output.accept(ModItems.T21_HELMET.get());
                        output.accept(ModItems.FAST_HELMET.get());
                        output.accept(ModItems.SNIPER21_HELMET.get());
                        output.accept(ModItems.PANTS21.get());
                        output.accept(ModItems.MSV_PANTS.get());
                        output.accept(ModItems.DESERT07_HELMET.get());
                        output.accept(ModItems.DESERT07_CHEST.get());
                        output.accept(ModItems.DESERT07_PANTS.get());
                        output.accept(ModItems.OCEAN07_HELMET.get());
                        output.accept(ModItems.OCEAN07_CHEST.get());
                        output.accept(ModItems.OCEAN07_PANTS.get());
                        output.accept(ModItems.UN_HELMET.get());
                        output.accept(ModItems.KR06_HELMET.get());
                        output.accept(ModItems.KR06_CHEST.get());
                        output.accept(ModItems.KR06_PANTS.get());

//                        output.accept(ModItems.TERRORIST_SPAWN_EGG.get());
                        output.accept(ModItems.SPRAY_CAN.get());
                        output.accept(ModItems.KEVLAR.get());
                        output.accept(ModItems.PAK40_DEPLOYER.get());
//                        output.accept(ModItems.GENERATOR_DEPLOYER.get());
                    }
                    ).build()
    );
}