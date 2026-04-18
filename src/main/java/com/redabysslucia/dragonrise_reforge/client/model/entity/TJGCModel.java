package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.F16CEntity;
import com.redabysslucia.dragonrise_reforge.entities.TJGCEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class TJGCModel extends VehicleModel<TJGCEntity> {

    @Override
    public @Nullable TransformContext<TJGCEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "root" -> (bone, vehicle, state) ->
                    bone.setHidden(hideForTurretControllerWhileZooming && vehicle.getWeaponIndex(0) == 2);

            case "wingLR" -> (bone, vehicle, state) ->
                    bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.flap1LRotO, vehicle.getFlap1LRot()) * Mth.DEG_TO_RAD);

            case "wingRR" -> (bone, vehicle, state) ->
                    bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.flap1RRotO, vehicle.getFlap1RRot()) * Mth.DEG_TO_RAD);

            case "wingLR2" -> (bone, vehicle, state) ->
                    bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.flap1L2RotO, vehicle.getFlap1L2Rot()) * Mth.DEG_TO_RAD);

            case "wingRR2" -> (bone, vehicle, state) ->
                    bone.setRotX(1.5f * Mth.lerp(state.getPartialTick(), vehicle.flap1R2RotO, vehicle.getFlap1R2Rot()) * Mth.DEG_TO_RAD);

            case "wingLB" -> (bone, vehicle, state) ->
                    bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.flap2LRotO, vehicle.getFlap2LRot()) * Mth.DEG_TO_RAD);

            case "wingRB" -> (bone, vehicle, state) ->
                    bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.flap2RRotO, vehicle.getFlap2RRot()) * Mth.DEG_TO_RAD);

            case "weiyiR" -> (bone, vehicle, state) ->
                    bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.flap3RotO, vehicle.getFlap3Rot()), -20f, 20f) * Mth.DEG_TO_RAD);

            case "gear", "gear2", "gear3" ->
                    (bone, vehicle, state) -> bone.setRotX(vehicle.gearRot(state.getPartialTick()) * Mth.DEG_TO_RAD);

            case "qianzhou", "qianzhou2" ->
                    (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.propellerRotO, vehicle.getPropellerRot()));

            case "bomb1" -> (bone, vehicle, state) ->
                    bone.setHidden(shouldHideBomb(vehicle, 1));

            case "bomb2" -> (bone, vehicle, state) ->
                    bone.setHidden(shouldHideBomb(vehicle, 2));

            // Missile2 (aim120) 1-20
            case "aim120-1" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 1));
            case "aim120-2" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 2));
            case "aim120-3" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 3));
            case "aim120-4" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 4));
            case "aim120-5" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 5));
            case "aim120-6" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 6));
            case "aim120-7" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 7));
            case "aim120-8" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 8));
            case "aim120-9" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 9));
            case "aim120-10" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 10));
            case "aim120-11" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 11));
            case "aim120-12" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 12));
            case "aim120-13" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 13));
            case "aim120-14" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 14));
            case "aim120-15" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 15));
            case "aim120-16" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 16));
            case "aim120-17" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 17));
            case "aim120-18" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 18));
            case "aim120-19" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 19));
            case "aim120-20" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 20));

            // SeekMissile (agm65) 1-5
            case "agm65-1" -> (bone, vehicle, state) -> bone.setHidden(shouldHideSeekMissile(vehicle, 1));
            case "agm65-2" -> (bone, vehicle, state) -> bone.setHidden(shouldHideSeekMissile(vehicle, 2));
            case "agm65-3" -> (bone, vehicle, state) -> bone.setHidden(shouldHideSeekMissile(vehicle, 3));
            case "agm65-4" -> (bone, vehicle, state) -> bone.setHidden(shouldHideSeekMissile(vehicle, 4));
            case "agm65-5" -> (bone, vehicle, state) -> bone.setHidden(shouldHideSeekMissile(vehicle, 5));

            // Missile 1-50
            case "bone9" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
            case "bone155" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
            case "bone98" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 3));
            case "bone136" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 4));
            case "bone104" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 5));
            case "bone83" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 6));
            case "bone109" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 7));
            case "bone46" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 8));
            case "bone26" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 9));
            case "bone161" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 10));
            case "bone56" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 11));
            case "bone142" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 12));
            case "bone38" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 13));
            case "bone129" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 14));
            case "bone92" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 15));
            case "bone64" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 16));
            case "bone32" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 17));
            case "bone167" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 18));
            case "bone50" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 19));
            case "bone148" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 20));
            case "bone68" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 21));
            case "bone123" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 22));
            case "bone86" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 23));
            case "bone76" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 24));
            case "bone336" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 25));
            case "bone348" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 26));
            case "bone241" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 27));
            case "bone318" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 28));
            case "bone222" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 29));
            case "bone299" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 30));
            case "bone203" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 31));
            case "bone280" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 32));
            case "bone184" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 33));
            case "bone261" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 34));
            case "bone247" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 35));
            case "bone324" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 36));
            case "bone228" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 37));
            case "bone305" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 38));
            case "bone214" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 39));
            case "bone292" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 40));
            case "bone190" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 41));
            case "bone267" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 42));
            case "bone253" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 43));
            case "bone330" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 44));
            case "bone234" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 45));
            case "bone331" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 46));
            case "bone209" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 47));
            case "bone286" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 48));
            case "bone196" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 49));
            case "bone273" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 50));

            default -> null;
        };
    }

    public boolean shouldHideBomb(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("Bomb");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("Missile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    public boolean shouldHideMissile2(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("Missile2");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    public boolean shouldHideSeekMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("SeekMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }
}