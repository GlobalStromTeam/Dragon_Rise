// Adapted from Frontline Combat Pack (Commissar_Mado), commit e4188472.
// Original license: META-INF/licenses/frontline-combat-pack.txt.
package com.redabysslucia.dragonrise_reforge.client;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.init.ModKeyMappings;
import com.redabysslucia.dragonrise_reforge.entity.vehicle.engine.EngineControl;
import com.redabysslucia.dragonrise_reforge.network.EngineControlPacket;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The driver's engine key (default I).
 *
 * Engine off: hold to start - HOLD goes to the server every tick while the key is down, and
 * letting go before the engine catches aborts the start. Engine running: hold for half a second to shut it down.
 * A press that stopped the engine has to be released before the next start.
 *
 * Also times the start for the HUD bar, from when this client saw the engine enter STARTING.
 */
@Mod.EventBusSubscriber(modid = "dragonrise_reforge", value = Dist.CLIENT)
public final class EngineKeyHandler {

    /** Ticks the key must be held to shut the engine down. */
    private static final int STOP_HOLD_TICKS = 10;

    private static boolean wasDown;
    private static VehicleEntity previousVehicle;
    /** Ticks the current press has been held while running, toward a shutdown; -1 when not counting. */
    private static int stopHeld = -1;
    /** True while the current press is a start hold (began with the engine off). */
    private static boolean starting;

    private static VehicleEntity hudVehicle;
    private static byte hudState = -1;
    private static int startObservedTick;

    private EngineKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        VehicleEntity vehicle = player != null && player.getVehicle() instanceof VehicleEntity v
                && v.getFirstPassenger() == player && EngineControl.appliesTo(v) ? v : null;

        if (vehicle != previousVehicle) {
            wasDown = ModKeyMappings.ENGINE_TOGGLE.getKeybind().isDown();
            starting = false;
            stopHeld = -1;
            previousVehicle = vehicle;
        }
        trackStart(vehicle);

        boolean down = vehicle != null && mc.screen == null && ModKeyMappings.ENGINE_TOGGLE.getKeybind().isDown();
        if (down && !wasDown) {
            byte state = EngineControl.state(vehicle);
            if (state == EngineControl.RUNNING) {
                stopHeld = 0;
                starting = false;
            } else {
                starting = true;
            }
        }
        if (!down) {
            starting = false;
            stopHeld = -1;
        }
        if (stopHeld >= 0 && ++stopHeld >= STOP_HOLD_TICKS) {
            ModNetwork.PACKET_HANDLER.sendToServer(new EngineControlPacket(EngineControlPacket.STOP));
            stopHeld = -1;
        }
        // Keep the start alive until it catches; after that the hold no longer matters.
        if (starting && EngineControl.state(vehicle) != EngineControl.RUNNING) {
            ModNetwork.PACKET_HANDLER.sendToServer(new EngineControlPacket(EngineControlPacket.HOLD));
        } else {
            starting = false;
        }
        wasDown = down;
    }

    private static void trackStart(VehicleEntity vehicle) {
        byte state = vehicle == null ? -1 : EngineControl.state(vehicle);
        if (vehicle != hudVehicle || state != hudState) {
            if (state == EngineControl.STARTING) startObservedTick = vehicle.tickCount;
            hudVehicle = vehicle;
            hudState = state;
        }
    }

    /** 0..1 progress of the engine start, for the HUD bar. */
    public static float startProgress(VehicleEntity vehicle, float partialTick) {
        if (vehicle != hudVehicle || hudState != EngineControl.STARTING) return 0f;
        float elapsed = vehicle.tickCount - startObservedTick + partialTick;
        return Mth.clamp(elapsed / EngineControl.startTicks(vehicle), 0f, 1f);
    }

}
