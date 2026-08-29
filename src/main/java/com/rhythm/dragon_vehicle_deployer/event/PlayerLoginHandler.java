package com.rhythm.dragon_vehicle_deployer.event;

import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = DragonVehicleDeployer.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerLoginHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Class<?> vehicleEntityClass = null;
    private static boolean classLookupDone = false;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) return;

        String playerUuid = serverPlayer.getStringUUID();

        // Lazy-load VehicleEntity class from SuperbWarfare at runtime
        if (!classLookupDone) {
            classLookupDone = true;
            try {
                vehicleEntityClass = Class.forName("com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity");
            } catch (ClassNotFoundException e) {
                LOGGER.warn("SuperbWarfare VehicleEntity class not found, vehicle cleanup on login disabled");
            }
        }

        if (vehicleEntityClass == null) return;

        List<Entity> toRemove = new ArrayList<>();
        for (Entity entity : serverLevel.getAllEntities()) {
            if (!entity.isAlive()) continue;
            if (!vehicleEntityClass.isInstance(entity)) continue;

            // 从实体NBT中读取最后驾驶者UUID
            CompoundTag tag = new CompoundTag();
            entity.saveWithoutId(tag);
            String lastDriver = tag.getString("LastDriver");

            if (playerUuid.equals(lastDriver)) {
                toRemove.add(entity);
            }
        }

        for (Entity entity : toRemove) {
            LOGGER.info("Removing vehicle {} (type={}) owned by player {} on login",
                    entity.getStringUUID(),
                    entity.getType().builtInRegistryHolder().key().location(),
                    serverPlayer.getName().getString());
            entity.discard();
        }
    }
}
