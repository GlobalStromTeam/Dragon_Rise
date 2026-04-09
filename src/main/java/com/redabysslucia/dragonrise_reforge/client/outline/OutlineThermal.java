package com.redabysslucia.dragonrise_reforge.client.outline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import com.redabysslucia.dragonrise_reforge.client.outline.render.OutlineRenderer;
import com.redabysslucia.dragonrise_reforge.entities.utils.INightVisionVehicle;

/**
 * Outline rendering system for thermal vision and night vision
 */
public class OutlineThermal {

    /**
     * Initialize outline rendering with custom settings
     * Call this during your mod initialization
     */
    public static void setupOutlines() {

        registerVehicleModeListener();
        OutlineRenderer.setUseBlackOutline(false);
        OutlineRenderer.setOutlinePredicate(entity -> {
            // 先检查排除的类型
            if (entity instanceof ItemEntity) return false;
            if (entity instanceof ThrowableProjectile) return false;
            if (entity instanceof HangingEntity) return false;
            if (entity instanceof ArmorStand) return false;
            
            // 允许所有非排除类型的实体，包括 geo 模型实体
            // 不再限制为 LivingEntity 类型
            return true;
        });
    }

    public static void registerVehicleModeListener() {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END) return;

            updateModeFromVehicle();
        });
    }

    public static void updateModeFromVehicle() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            OutlineRenderer.setRenderMode(OutlineRenderer.RenderMode.OFF);
            return;
        }

        OutlineRenderer.setOutlineColorProvider(null);

        // Check if player is in a vehicle with night vision or thermal vision
        if (player.getVehicle() instanceof INightVisionVehicle vehicle) {
            System.out.println("Player in vehicle, NV: " + vehicle.getNVEnable() + ", TVG: " + vehicle.getTVGEnable());
            if (vehicle.getTVGEnable() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                // Thermal vision mode - enable glowing
                System.out.println("Enabling thermal vision overlay");
                OutlineRenderer.setRenderMode(OutlineRenderer.RenderMode.OVERLAY);
                OutlineRenderer.setOutlineColor(1.0f, 1.0f, 1.0f, 2.0f);
            }
            else if (vehicle.getNVEnable() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                // Night vision mode - disable glowing
                System.out.println("Disabling outline rendering for night vision");
                OutlineRenderer.setRenderMode(OutlineRenderer.RenderMode.OFF);
            }
            else {
                // No vision mode enabled
                System.out.println("Disabling outline rendering");
                OutlineRenderer.setRenderMode(OutlineRenderer.RenderMode.OFF);
            }
        }
        else {
            // Not in a vehicle
            System.out.println("Not in vehicle, disabling outline rendering");
            OutlineRenderer.setRenderMode(OutlineRenderer.RenderMode.OFF);
        }
    }
}