package com.redabysslucia.dragonrise_reforge.datagen;

import com.atsuishio.superbwarfare.data.loot.WreckageLootData;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModItems;
import com.redabysslucia.dragonrise_reforge.datagen.base.SbwWreckageLootProvider;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModWreckageLootProvider extends SbwWreckageLootProvider {

    public ModWreckageLootProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper);
    }

    @Override
    public void generate() {
        // 主战坦克
        add(ModEntities.ZTZ99A.get(), createTankLoot(6, 1));
        add(ModEntities.ZTZ99BH.get(), createTankLoot(8, 1));
        add(ModEntities.M1A2SEPV2.get(), createTankLoot(6, 1));
        add(ModEntities.T80.get(), createTankLoot(5, 1));
        add(ModEntities.T80B.get(), createTankLoot(5, 1));
        add(ModEntities.AMX56.get(), createTankLoot(6, 1));
        add(ModEntities.T90MH.get(), createTankLoot(6, 1));
        add(ModEntities.L1A2.get(), createTankLoot(6, 1));
        add(ModEntities.VT4A1.get(), createTankLoot(5, 1));
        add(ModEntities.VT4B.get(), createTankLoot(5, 1));
        add(ModEntities.ZTZ59A.get(), createTankLoot(4, 1));
        add(ModEntities.T3476.get(), createTankLoot(3, 1));
        add(ModEntities.IS2.get(), createTankLoot(4, 1));
        add(ModEntities.TIGER.get(), createTankLoot(4, 1));
        add(ModEntities.PANZER4.get(), createTankLoot(3, 1));
        add(ModEntities.M4A2.get(), createTankLoot(3, 1));
        add(ModEntities.M4A2105.get(), createTankLoot(3, 1));

        // 轻型坦克/装甲车
        add(ModEntities.ZTQ15.get(), createLightTankLoot(4));
        add(ModEntities.M10BOOKER.get(), createLightTankLoot(4));
        add(ModEntities.R2S25M.get(), createLightTankLoot(3));
        add(ModEntities.BMP3.get(), createLightTankLoot(3));
        add(ModEntities.BMPT72.get(), createLightTankLoot(4));
        add(ModEntities.ZBD04A.get(), createLightTankLoot(3));
        add(ModEntities.ZBL08.get(), createLightTankLoot(3));
        add(ModEntities.CM34.get(), createLightTankLoot(3));
        add(ModEntities.CV90.get(), createLightTankLoot(3));
        add(ModEntities.M3A3.get(), createLightTankLoot(3));
        add(ModEntities.M3Stuart.get(), createLightTankLoot(3));
        add(ModEntities.CSK181.get(), createLightVehicleLoot(2));
        add(ModEntities.BMD4M.get(), createLightVehicleLoot(2));
        add(ModEntities.LVT.get(), createLightVehicleLoot(2));
        add(ModEntities.ZLT_11.get(), createLightVehicleLoot(3));
        add(ModEntities.SD905.get(), createLightVehicleLoot(3));

        // 自行火炮/防空
        add(ModEntities.S2S38.get(), createArtilleryLoot(5));
        add(ModEntities.ZSU234.get(), createArtilleryLoot(4));
        add(ModEntities.AA625E.get(), createArtilleryLoot(4));
        add(ModEntities.PROJECT640.get(), createArtilleryLoot(4));
        add(ModEntities.WLHGZU23.get(), createArtilleryLoot(2));
        add(ModEntities.WLSC.get(), createArtilleryLoot(3));

        // 轻型车辆
        add(ModEntities.SX1.get(), createLightVehicleLoot(2));
        add(ModEntities.FAVA.get(), createLightVehicleLoot(2));
        add(ModEntities.PZBJY.get(), createLightVehicleLoot(2));
        add(ModEntities.STRV103.get(), createLightVehicleLoot(3));
        add(ModEntities.type3.get(), createLightVehicleLoot(2));
        add(ModEntities.type97.get(), createLightVehicleLoot(2));
        add(ModEntities.type97Q.get(), createLightVehicleLoot(2));
        add(ModEntities.TOYOTASEIKI.get(), createLightVehicleLoot(1));
        add(ModEntities.motuo.get(), createLightVehicleLoot(1));
        add(ModEntities.SHIELD.get(), createLightVehicleLoot(1));
        add(ModEntities.MK19.get(), createLightVehicleLoot(1));
        add(ModEntities.ZU23.get(), createLightVehicleLoot(1));
        add(ModEntities.DSHK.get(), createLightVehicleLoot(1));
        add(ModEntities.M2.get(), createLightVehicleLoot(1));
        add(ModEntities.qjz89.get(), createLightVehicleLoot(1));
        add(ModEntities.AKM.get(), createLightVehicleLoot(1));

        // 武装直升机
        add(ModEntities.AH64.get(), createHelicopterLoot(4));
        add(ModEntities.Z10A.get(), createHelicopterLoot(4));
        add(ModEntities.Z10ME.get(), createHelicopterLoot(5));
        add(ModEntities.EC665.get(), createHelicopterLoot(4));
        add(ModEntities.KA50.get(), createHelicopterLoot(4));
        add(ModEntities.Z9.get(), createHelicopterLoot(3));
        add(ModEntities.NH90.get(), createHelicopterLoot(3));
        add(ModEntities.Z20.get(), createHelicopterLoot(3));
        add(ModEntities.UH60.get(), createHelicopterLoot(3));

        // 固定翼飞机
        add(ModEntities.J10.get(), createAircraftLoot(5));
        add(ModEntities.J10C.get(), createAircraftLoot(6));
        add(ModEntities.J11.get(), createAircraftLoot(5));
        add(ModEntities.J15T.get(), createAircraftLoot(5));
        add(ModEntities.J16.get(), createAircraftLoot(6));
        add(ModEntities.J20.get(), createAircraftLoot(8));
        add(ModEntities.J20VTOL.get(), createAircraftLoot(8));
        add(ModEntities.J35.get(), createAircraftLoot(5));
        add(ModEntities.J8.get(), createAircraftLoot(4));
        add(ModEntities.JF17.get(), createAircraftLoot(4));
        add(ModEntities.JAS39E.get(), createAircraftLoot(5));
        add(ModEntities.Q5.get(), createAircraftLoot(4));
        add(ModEntities.F14.get(), createAircraftLoot(6));
        add(ModEntities.F16C.get(), createAircraftLoot(5));
        add(ModEntities.F4U.get(), createAircraftLoot(3));
        add(ModEntities.REFALE.get(), createAircraftLoot(5));
        add(ModEntities.REFALEAA.get(), createAircraftLoot(6));
        add(ModEntities.AV8B.get(), createAircraftLoot(4));
        add(ModEntities.syy651.get(), createAircraftLoot(4));
        add(ModEntities.HYR0.get(), createAircraftLoot(6));

        // 特种车辆
        add(ModEntities.CYBORG_TANK.get(), createSpecialLoot(8));
        add(ModEntities.TJGC.get(), createSpecialLoot(6));

        // 特殊装备
        add(ModEntities.TEST.get(), createDefaultLoot());
        add(ModEntities.SPACEBAG.get(), createDefaultLoot());
        add(ModEntities.CAMEL.get(), createDefaultLoot());
        add(ModEntities.NPDS114.get(), createDefaultLoot());
        add(ModEntities.NPDS514.get(), createDefaultLoot());
        add(ModEntities.NPDS810.get(), createDefaultLoot());
        add(ModEntities.TESTSHIP.get(), createDefaultLoot());
    }

    private WreckageLootData.Builder createTankLoot(int steelAmount, int moduleLevel) {
        var builder = new WreckageLootData.Builder();
        
        var pool1 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool1.source(ModDamageTypes.REPAIR_TOOL);
        pool1.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 1.0));
        
        if (moduleLevel >= 1) {
            pool1.addEntry(new WreckageLootData.Entry(ModItems.MEDIUM_ARMAMENT_MODULE.get(), 1, 0.5));
        }
        if (moduleLevel >= 2) {
            pool1.addEntry(new WreckageLootData.Entry(ModItems.HEAVY_ARMAMENT_MODULE.get(), 1, 0.3));
        }
        
        pool1.addEntry(new WreckageLootData.Entry(ModItems.TRACK.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_MOTOR.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.MEDIUM_BATTERY_PACK.get(), 1, 0.2));
        
        var pool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 0.5));
        
        builder.addPool(pool1.build());
        builder.addPool(pool2.build());
        
        // 添加炮塔和车体分离的战利品池
        var turretPool = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.TURRET_ONLY);
        turretPool.source(ModDamageTypes.REPAIR_TOOL);
        turretPool.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), Math.max(1, steelAmount / 2), 1.0));
        if (moduleLevel >= 1) {
            turretPool.addEntry(new WreckageLootData.Entry(ModItems.MEDIUM_ARMAMENT_MODULE.get(), 1, 0.5));
        }
        
        var turretPool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.TURRET_ONLY);
        turretPool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), Math.max(1, steelAmount / 2), 0.5));
        
        var vehiclePool = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.VEHICLE_ONLY);
        vehiclePool.source(ModDamageTypes.REPAIR_TOOL);
        vehiclePool.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), Math.max(1, steelAmount / 2), 1.0));
        vehiclePool.addEntry(new WreckageLootData.Entry(ModItems.TRACK.get(), 1, 0.5));
        vehiclePool.addEntry(new WreckageLootData.Entry(ModItems.LARGE_MOTOR.get(), 1, 0.5));
        vehiclePool.addEntry(new WreckageLootData.Entry(ModItems.MEDIUM_BATTERY_PACK.get(), 1, 0.2));
        
        var vehiclePool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.VEHICLE_ONLY);
        vehiclePool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), Math.max(1, steelAmount / 2), 0.5));
        
        builder.addPool(turretPool.build());
        builder.addPool(turretPool2.build());
        builder.addPool(vehiclePool.build());
        builder.addPool(vehiclePool2.build());
        
        return builder;
    }

    private WreckageLootData.Builder createLightTankLoot(int steelAmount) {
        var builder = new WreckageLootData.Builder();
        var pool1 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool1.source(ModDamageTypes.REPAIR_TOOL);
        pool1.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 1.0));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LIGHT_ARMAMENT_MODULE.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.WHEEL.get(), 2, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_MOTOR.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.MEDIUM_BATTERY_PACK.get(), 1, 0.2));
        
        var pool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 0.5));
        
        builder.addPool(pool1.build());
        builder.addPool(pool2.build());
        return builder;
    }

    private WreckageLootData.Builder createArtilleryLoot(int steelAmount) {
        var builder = new WreckageLootData.Builder();
        var pool1 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool1.source(ModDamageTypes.REPAIR_TOOL);
        pool1.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 1.0));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.CANNON_CORE.get(), 2, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.TRACK.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_MOTOR.get(), 1, 0.5));
        
        var pool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 0.5));
        
        builder.addPool(pool1.build());
        builder.addPool(pool2.build());
        return builder;
    }

    private WreckageLootData.Builder createHelicopterLoot(int steelAmount) {
        var builder = new WreckageLootData.Builder();
        var pool1 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool1.source(ModDamageTypes.REPAIR_TOOL);
        pool1.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 1.0));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.MEDIUM_ARMAMENT_MODULE.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_PROPELLER.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_MOTOR.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_BATTERY_PACK.get(), 1, 0.5));
        
        var pool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 0.5));
        
        builder.addPool(pool1.build());
        builder.addPool(pool2.build());
        return builder;
    }

    private WreckageLootData.Builder createAircraftLoot(int steelAmount) {
        var builder = new WreckageLootData.Builder();
        var pool1 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool1.source(ModDamageTypes.REPAIR_TOOL);
        pool1.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 1.0));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.HEAVY_ARMAMENT_MODULE.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_PROPELLER.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_MOTOR.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_BATTERY_PACK.get(), 1, 0.5));
        
        var pool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 0.5));
        
        builder.addPool(pool1.build());
        builder.addPool(pool2.build());
        return builder;
    }

    private WreckageLootData.Builder createSpecialLoot(int steelAmount) {
        var builder = new WreckageLootData.Builder();
        var pool1 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool1.source(ModDamageTypes.REPAIR_TOOL);
        pool1.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 1.0));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.HEAVY_ARMAMENT_MODULE.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LASER_UNIT.get(), 4, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(Items.NETHERITE_BLOCK, 2, 0.3));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.CEMENTED_CARBIDE_BLOCK.get(), 4, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_MOTOR.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_BATTERY_PACK.get(), 1, 0.5));
        
        var pool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 0.5));
        pool2.addEntry(new WreckageLootData.Entry(Items.NETHERITE_BLOCK, 1, 0.15));
        
        builder.addPool(pool1.build());
        builder.addPool(pool2.build());
        return builder;
    }

    private WreckageLootData.Builder createLightVehicleLoot(int steelAmount) {
        var builder = new WreckageLootData.Builder();
        var pool1 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool1.source(ModDamageTypes.REPAIR_TOOL);
        pool1.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 1.0));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.WHEEL.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.LARGE_MOTOR.get(), 1, 0.5));
        pool1.addEntry(new WreckageLootData.Entry(ModItems.SMALL_BATTERY_PACK.get(), 1, 0.2));
        
        var pool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), steelAmount, 0.5));
        
        builder.addPool(pool1.build());
        builder.addPool(pool2.build());
        return builder;
    }

    private WreckageLootData.Builder createDefaultLoot() {
        var builder = new WreckageLootData.Builder();
        var pool1 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool1.source(ModDamageTypes.REPAIR_TOOL);
        pool1.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), 1, 1.0));
        
        var pool2 = new WreckageLootData.Pool.Builder(1, "@Default", WreckageLootData.Pool.Type.COMPLETE);
        pool2.addEntry(new WreckageLootData.Entry(ModItems.STEEL_BLOCK.get(), 1, 0.2));
        
        builder.addPool(pool1.build());
        builder.addPool(pool2.build());
        return builder;
    }
}
