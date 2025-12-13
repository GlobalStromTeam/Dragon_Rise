package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.DataLoader;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import lombok.Setter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;


public abstract class VariableEngineVehicle extends GeoVehicleEntity {

    //public static final EntityDataAccessor<String> FLY_MODE = SynchedEntityData.defineId(VTOLEntity.class, EntityDataSerializers.STRING);
    private EngineInfo variableEngineCache;

    @Setter
    private List<EngineType> engineTypeList;

    private int engineTypeIndex = 0;


    public VariableEngineVehicle(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void toggleChangeMode(){
        engineTypeIndex = (engineTypeIndex + 1) % engineTypeList.size();
        var newMode = engineTypeList.get(engineTypeIndex);
        updateEngineCache(newMode);
    }

    public void updateEngineCache(EngineType pEngineType) {
        var computed = computed();
        var engineInfo = computed.engineInfo;
        try {
            this.variableEngineCache = switch (pEngineType) {
                case WHEEL -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Wheel.class);
                case TRACK -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Track.class);
                case HELICOPTER -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Helicopter.class);
                case SHIP -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Ship.class);
                case AIRCRAFT -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Aircraft.class);
                case WHEELCHAIR -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.WheelChair.class);
                case TOM6 -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Tom6.class);
                default -> null;
            };
            //setFlyMode(engineType.toString());
        } catch (Exception e) {
            Mod.LOGGER.error("Failed to parse engine info for vehicle {}, {}", this, e);
        }
    }

    @Override
    public void travel(){
        var computed = computed();

        var engineType = computed.engineType;
        if (engineType == EngineType.EMPTY) return;
        if (engineType == EngineType.FIXED) {
            this.fixedEngine();
            return;
        }

        if (variableEngineCache == null) {
            var engineInfo = computed.engineInfo;
            try {
                //this.variableEngineCache = DataLoader.GSON.fromJson(engineInfo, EngineInfo.Helicopter.class);
                this.variableEngineCache = switch (engineType) {
                    case WHEEL -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Wheel.class);
                    case TRACK -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Track.class);
                    case HELICOPTER -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Helicopter.class);
                    case SHIP -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Ship.class);
                    case AIRCRAFT -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Aircraft.class);
                    case WHEELCHAIR -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.WheelChair.class);
                    case TOM6 -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Tom6.class);
                    default -> null;
                };
                //setFlyMode(engineType.toString());
            } catch (Exception e) {
                Mod.LOGGER.error("Failed to parse engine info for vehicle {}, {}", this, e);
            }
        } else {
            //if(this.getFlyMode().equals("VTOL"))
                this.variableEngineCache.work(this);
//            else{
//                this.engineCacheMode2.work(this);
//            }
        }
    }

}
