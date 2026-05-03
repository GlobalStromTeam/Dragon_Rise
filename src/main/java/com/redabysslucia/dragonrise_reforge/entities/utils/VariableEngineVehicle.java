package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.DataLoader;
import com.atsuishio.superbwarfare.data.vehicle.DefaultVehicleData;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.FireLightVisionVehicle;
import com.redabysslucia.dragonrise_reforge.init.ModSpeedSounds;
import com.redabysslucia.dragonrise_reforge.utils.SpeedSoundUtil;
import lombok.Setter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Math;

import java.util.List;


public abstract class VariableEngineVehicle extends NightVisionVehicle {

    //public static final EntityDataAccessor<String> FLY_MODE = SynchedEntityData.defineId(VTOLEntity.class, EntityDataSerializers.STRING);
    private EngineInfo variableEngineCache;

    //@Setter
    private List<EngineType> engineTypeList;
    private EngineType currentEngineType;

    private int engineTypeIndex = 0;


    public VariableEngineVehicle(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 检查速度并播放音效
        SpeedSoundUtil.checkSpeedAndPlaySound(this, ModSpeedSounds.PLANE_HIGH_SPEED.get(), ModSpeedSounds.PLANE_SUPERSONIC_LOOP.get());
    }

    public void setEngineTypeList(List<EngineType> engineTypeList) {
        this.engineTypeList = engineTypeList;
        this.currentEngineType = engineTypeList.get(engineTypeList.size() - 1);
    }

    public void toggleChangeMode() {
        engineTypeIndex = (engineTypeIndex + 1) % engineTypeList.size();
        var newMode = engineTypeList.get(engineTypeIndex);
        updateEngineCache(newMode);
    }

    public void updateEngineCache(EngineType pEngineType) {
        var computed = computed();
        var engineInfo = computed.engineInfo;
        try {
            if (currentEngineType == EngineType.HELICOPTER) {
                entityData.set(POWER, entityData.get(POWER) / 0.12f);
            }
            this.variableEngineCache = switch (pEngineType) {
                case WHEEL -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Wheel.class);
                case TRACK -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Track.class);
                case HELICOPTER -> {
                    entityData.set(POWER, entityData.get(POWER) * 0.12f);
                    yield DataLoader.GSON.fromJson(engineInfo, EngineInfo.Helicopter.class);
                }
                case SHIP -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Ship.class);
                case AIRCRAFT -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Aircraft.class);
                case WHEELCHAIR -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.WheelChair.class);
                case TOM6 -> DataLoader.GSON.fromJson(engineInfo, EngineInfo.Tom6.class);
                default -> null;
            };
            currentEngineType = pEngineType;
            //setFlyMode(engineType.toString());
        } catch (Exception e) {
            Mod.LOGGER.error("Failed to parse engine info for vehicle {}, {}", this, e);
        }
    }

    @Override
    public void travel() {
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

    @Override
    public float getEngineSoundVolume() {
        //DefaultVehicleData computed = this.computed();
        EngineType engineType = currentEngineType;
        if (engineType != EngineType.EMPTY && engineType != EngineType.FIXED && engineType != null) {
            EngineInfo engineInfo = this.variableEngineCache;
            if (engineInfo == null) {
                return 0.0F;
            } else {
                float var10000;
                switch (engineType) {
                    case TRACK ->
                            var10000 = Math.max(Mth.abs((Float) this.entityData.get(POWER)), Mth.abs(1.4F * (Float) this.entityData.get(DELTA_ROT))) * engineInfo.engineSoundVolume;
                    case HELICOPTER ->
                            var10000 = (Float) this.entityData.get(POWER) / 0.12f * engineInfo.engineSoundVolume;
                    default -> var10000 = Mth.abs((Float) this.entityData.get(POWER)) * engineInfo.engineSoundVolume;
                }

                return var10000;
            }
        } else {
            return 0.0F;
        }
    }

}
