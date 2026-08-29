package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@SuppressWarnings("removal")
public class M113Entity extends SyncCameraVehicle implements IVehicleBackground {
    private float prevYRot;

        public M113Entity(EntityType<M113Entity> type, Level world) {
                super(type, world);
        }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getBackgroundTexture() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return null;

        var seatIndex = getSeatIndex(player);
        // 一号位背景
        if (seatIndex == 0) {
            return ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos2.png");
        }
        return null;
    }
}
