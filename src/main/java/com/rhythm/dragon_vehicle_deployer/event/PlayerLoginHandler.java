package com.rhythm.dragon_vehicle_deployer.event;

import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = DragonVehicleDeployer.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerLoginHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
}
