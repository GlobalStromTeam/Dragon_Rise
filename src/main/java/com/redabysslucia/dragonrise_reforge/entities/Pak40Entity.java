package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@SuppressWarnings("removal")
public class Pak40Entity extends SyncCameraVehicle implements IVehicleBackground {

        public Pak40Entity(EntityType<Pak40Entity> type, Level world) {
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
                if (seatIndex == 1) {
                        return ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/zf3x8.png");
                }
                return null;
        }
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean scaleByHeight() {
                return true;
        }
}
