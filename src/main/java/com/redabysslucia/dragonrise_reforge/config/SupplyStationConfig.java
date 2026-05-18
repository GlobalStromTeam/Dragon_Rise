package com.redabysslucia.dragonrise_reforge.config;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class SupplyStationConfig {

    @SerializedName("Default")
    public ResupplyRule defaultRule = new ResupplyRule();

    @SerializedName("VehicleOverrides")
    public Map<String, ResupplyRule> vehicleOverrides = new HashMap<>();

    public static class ResupplyRule {

        @SerializedName("Mode")
        public String mode = "MAGAZINE";

        @SerializedName("FixedAmount")
        public int fixedAmount = 100;

        @SerializedName("HealPercent")
        public float healPercent = 50f;

        @SerializedName("AmmoOverrides")
        public Map<String, AmmoTypeRule> ammoOverrides = new HashMap<>();
    }

    public static class AmmoTypeRule {

        @SerializedName("Mode")
        public String mode = "MAGAZINE";

        @SerializedName("FixedAmount")
        public int fixedAmount = 100;

        @SerializedName("CustomItem")
        public String customItem = "";

        @SerializedName("CustomItemCount")
        public int customItemCount = 1;
    }

    public ResupplyRule getRuleForVehicle(String vehicleId) {
        ResupplyRule override = vehicleOverrides.get(vehicleId);
        if (override != null) {
            return override;
        }
        return defaultRule;
    }

    public AmmoTypeRule getAmmoRule(ResupplyRule vehicleRule, String ammoKey) {
        AmmoTypeRule override = vehicleRule.ammoOverrides.get(ammoKey);
        if (override != null) {
            return override;
        }
        AmmoTypeRule defaultAmmoRule = new AmmoTypeRule();
        defaultAmmoRule.mode = vehicleRule.mode;
        defaultAmmoRule.fixedAmount = vehicleRule.fixedAmount;
        return defaultAmmoRule;
    }

    public boolean isMagazineMode(ResupplyRule rule, String ammoKey) {
        AmmoTypeRule ammoRule = getAmmoRule(rule, ammoKey);
        return "MAGAZINE".equalsIgnoreCase(ammoRule.mode);
    }

    public boolean isPackageBasedMode(ResupplyRule rule, String ammoKey) {
        AmmoTypeRule ammoRule = getAmmoRule(rule, ammoKey);
        return "PACKAGE".equalsIgnoreCase(ammoRule.mode);
    }
}
