package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.TJGCEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class TJGCModel extends DragonriseVehicleModel<TJGCEntity> {

    @Override
    public @Nullable TransformContext<TJGCEntity> collectTransform(String boneName) {
        return switch (boneName) {
            case "root" -> (bone, vehicle, state) ->
                    bone.setHidden(getHideForTurretControllerWhileZooming() && vehicle.getWeaponIndex(0) == 2);

            case "wingLR" -> (bone, vehicle, state) ->
                    bone.setRotX(0.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1LRotO(), vehicle.getFlap1LRot()) * Mth.DEG_TO_RAD);

            case "wingRR" -> (bone, vehicle, state) ->
                    bone.setRotX(0.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1RRotO(), vehicle.getFlap1RRot()) * Mth.DEG_TO_RAD);

            case "wingLR2" -> (bone, vehicle, state) ->
                    bone.setRotX(0.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1L2RotO(), vehicle.getFlap1L2Rot()) * Mth.DEG_TO_RAD);

            case "wingRR2" -> (bone, vehicle, state) ->
                    bone.setRotX(0.5f * Mth.lerp(state.getPartialTick(), vehicle.getFlap1R2RotO(), vehicle.getFlap1R2Rot()) * Mth.DEG_TO_RAD);

            case "wingLB" -> (bone, vehicle, state) ->
                    bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2LRotO(), vehicle.getFlap2LRot()) * Mth.DEG_TO_RAD);

            case "wingRB" -> (bone, vehicle, state) ->
                    bone.setRotX(Mth.lerp(state.getPartialTick(), vehicle.getFlap2RRotO(), vehicle.getFlap2RRot()) * Mth.DEG_TO_RAD);

            case "weiyiR" -> (bone, vehicle, state) ->
                    bone.setRotY(Mth.clamp(Mth.lerp(state.getPartialTick(), vehicle.getFlap3RotO(), vehicle.getFlap3Rot()), -20f, 20f) * Mth.DEG_TO_RAD);

            case "gear", "gear2", "gear3" ->
                    (bone, vehicle, state) -> bone.setRotX(vehicle.gearRot(state.getPartialTick()) * Mth.DEG_TO_RAD);

            case "qianzhou", "qianzhou2" ->
                    (bone, vehicle, state) -> bone.setRotZ(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));

            case "bone355" -> (bone, vehicle, state) ->
                    bone.setHidden(shouldHideBomb(vehicle, 1));

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
            case "bone311" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 46));
            case "bone209" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 47));
            case "bone286" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 48));
            case "bone196" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 49));
            case "bone273" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 50));

            case "aim120-16" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 1));
            case "aim120-21" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 2));
            case "aim120-14" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 3));
            case "aim120-19" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 4));
            case "aim120-13" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 5));
            case "aim120-17" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 6));
            case "aim120-12" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 7));
            case "aim120-18" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 8));
            case "aim120-15" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 9));
            case "aim120-20" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissile2(vehicle, 10));

            case "aim120-2" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 1));
            case "aim120-7" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 2));
            case "aim120-3" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 3));
            case "aim120-8" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 4));
            case "aim120-4" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 5));
            case "aim120-9" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 6));
            case "aim120-5" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 7));
            case "aim120-10" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 8));
            case "aim120-6" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 9));
            case "aim120-11" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAA(vehicle, 10));

            case "agm65-11" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 1));
            case "agm65-12" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 2));
            case "agm65-13" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 3));
            case "agm65-14" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 4));
            case "agm65-15" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 5));
            case "agm65-1" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 6));
            case "agm65-2" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 7));
            case "agm65-3" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 8));
            case "agm65-4" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 9));
            case "agm65-5" -> (bone, vehicle, state) -> bone.setHidden(shouldHideMissileAT(vehicle, 10));

            case "gbu24" -> (bone, vehicle, state) -> bone.setHidden(shouldHideBigATMissile(vehicle, 1));
            case "gbu2" -> (bone, vehicle, state) -> bone.setHidden(shouldHideBigATMissile(vehicle, 2));
            case "gbu3" -> (bone, vehicle, state) -> bone.setHidden(shouldHideBigATMissile(vehicle, 3));
            case "gbu4" -> (bone, vehicle, state) -> bone.setHidden(shouldHideBigATMissile(vehicle, 4));
            case "gbu5" -> (bone, vehicle, state) -> bone.setHidden(shouldHideBigATMissile(vehicle, 5));


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
        var gunData = vehicle.getGunData("Rocket");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    public boolean shouldHideMissile2(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("BigRocket");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    public boolean shouldHideMissileAA(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("AAMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    public boolean shouldHideMissileAT(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("ATMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    public boolean shouldHideBigATMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("BigATMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }
}
