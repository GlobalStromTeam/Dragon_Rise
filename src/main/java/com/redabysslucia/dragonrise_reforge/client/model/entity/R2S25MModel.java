package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.M10BookerEntity;
import com.redabysslucia.dragonrise_reforge.entities.R2S25MEntity;
import net.minecraft.util.Mth;

public class R2S25MModel extends DragonriseVehicleModel<R2S25MEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
    public float getBoneRotX(float t) {
        if (t <= 37.0833) return 0F;
        if (t <= 37.5) return Mth.lerp((t - 37.0833F) / (37.5F - 37.0833F), 0F, 7.5F);
        if (t <= 40.75) return 7.5F;
        if (t <= 41.1667) return Mth.lerp((t - 40.75F) / (41.1667F - 40.75F), 7.5F, -37.5F);
        if (t <= 42.4167) return -37.5F;
        if (t <= 42.8333) return Mth.lerp((t - 42.4167F) / (42.8333F - 42.4167F), -37.5F, -75F);
        if (t <= 43.6667) return -75F;
        if (t <= 44.0833) return Mth.lerp((t - 43.6667F) / (44.0833F - 43.6667F), -75F, -110F);
        if (t <= 44.9167) return -110F;
        if (t <= 45.4167) return Mth.lerp((t - 44.9167F) / (45.4167F - 44.9167F), -110F, -155F);
        if (t <= 53.3333) return -155F;
        if (t <= 53.8333) return Mth.lerp((t - 53.3333F) / (53.8333F - 53.3333F), -155F, -180F);
        if (t <= 82.3333) return -180F;
        if (t <= 82.8333) return Mth.lerp((t - 82.3333F) / (82.8333F - 82.3333F), -180F, -205F);
        if (t <= 90.3333) return -205F;
        if (t <= 90.75) return Mth.lerp((t - 90.3333F) / (90.75F - 90.3333F), -205F, -257.5F);
        if (t <= 92) return -257.5F;
        if (t <= 92.4167) return Mth.lerp((t - 92F) / (92.4167F - 92F), -257.5F, -282.5F);
        if (t <= 93.25) return -282.5F;
        if (t <= 93.6667) return Mth.lerp((t - 93.25F) / (93.6667F - 93.25F), -282.5F, -332.5F);
        if (t <= 94.8333) return -332.5F;
        if (t <= 95.25) return Mth.lerp((t - 94.8333F) / (95.25F - 94.8333F), -332.5F, -360F);

        return -360F;

    }
        public float getBoneMoveY ( float t){
            if (t <= 37) return 0F;
            if (t <= 41.1667) return Mth.lerp((t - 37F) / (41.1667F - 37F), 0F, 0.93F);
            if (t <= 42.8333) return Mth.lerp((t - 41.1667F) / (42.8333F - 41.1667F), 0.93F, -2.03F);
            if (t <= 44.0833) return Mth.lerp((t - 42.8333F) / (44.0833F - 42.8333F), -2.03F, -4.98F);
            if (t <= 45.4167) return Mth.lerp((t - 44.0833F) / (45.4167F - 44.0833F), -4.98F, -8.86F);
            if (t <= 53.8333) return Mth.lerp((t - 45.4167F) / (53.8333F - 45.4167F), -8.86F, -16.88F);
            if (t <= 82.8333) return Mth.lerp((t - 53.8333F) / (82.8333F - 53.8333F), -16.88F, -16.87F);
            if (t <= 90.8333) return Mth.lerp((t - 82.8333F) / (90.8333F - 82.8333F), -16.87F, -8.7F);
            if (t <= 92.4167) return Mth.lerp((t - 90.8333F) / (92.4167F - 90.8333F), -8.7F, -4.87F);
            if (t <= 93.6667) return Mth.lerp((t - 92.4167F) / (93.6667F - 92.4167F), -4.87F, -1.05F);
            if (t <= 94.8333) return Mth.lerp((t - 93.6667F) / (94.8333F - 93.6667F), -1.05F, -0.03F);

            return Mth.lerp(t - 94.8333F, -0.03F, 0F);
        }
        public float getBoneMoveZ ( float t){
            if (t <= 37) return Mth.lerp(t / 37F, 0F, 84F);
            if (t <= 41.1667) return Mth.lerp((t - 37F) / (41.1667F - 37F), 84F, 95.13F);
            if (t <= 42.8333) return Mth.lerp((t - 41.1667F) / (42.8333F - 41.1667F), 95.13F, 99.22F);
            if (t <= 44.0833) return Mth.lerp((t - 42.8333F) / (44.0833F - 42.8333F), 99.22F, 99.83F);
            if (t <= 45.4167) return Mth.lerp((t - 44.0833F) / (45.4167F - 44.0833F), 99.83F, 98.4F);
            if (t <= 53.8333) return Mth.lerp((t - 45.4167F) / (53.8333F - 45.4167F), 98.4F, 79.97F);
            if (t <= 82.8333) return Mth.lerp((t - 53.8333F) / (82.8333F - 53.8333F), 79.97F, 10.5F);
            if (t <= 90.8333) return Mth.lerp((t - 82.8333F) / (90.8333F - 82.8333F), 10.5F, -5.75F);
            if (t <= 92.4167) return Mth.lerp((t - 90.8333F) / (92.4167F - 90.8333F), -5.75F, -6.88F);
            if (t <= 93.6667) return Mth.lerp((t - 92.4167F) / (93.6667F - 92.4167F), -6.88F, -6.31F);
            if (t <= 94.8333) return Mth.lerp((t - 93.6667F) / (94.8333F - 93.6667F), -6.31F, -3.19F);

            return Mth.lerp(t - 94.8333F, -3.19F, 0F);

        }
    }