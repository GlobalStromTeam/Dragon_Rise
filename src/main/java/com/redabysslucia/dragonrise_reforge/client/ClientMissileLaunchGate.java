package com.redabysslucia.dragonrise_reforge.client;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.GroundMissileGuidance;
import com.redabysslucia.dragonrise_reforge.entities.utils.ClientLaunchReservations;
import com.redabysslucia.dragonrise_reforge.network.message.MissileLaunchStateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.lang.ref.WeakReference;
import java.util.UUID;

/** Local prediction prevents duplicate trigger audio; the server independently rejects duplicate launches. */
public final class ClientMissileLaunchGate {
    private static final ClientLaunchReservations<UUID> RESERVATIONS = new ClientLaunchReservations<>();
    private static WeakReference<ClientLevel> lastLevel = new WeakReference<>(null);

    private ClientMissileLaunchGate() {}

    private static long now(ClientLevel level) {
        if (level != lastLevel.get()) {
            RESERVATIONS.clear();
            lastLevel = new WeakReference<>(level);
        }
        return level.getGameTime();
    }

    public static boolean tryBegin(VehicleEntity vehicle, GunData gun) {
        if (!GroundMissileGuidance.delayedMissile(vehicle, gun)) return true;
        long tick = now((ClientLevel) vehicle.level());
        // Only unconfirmed prediction times out (e.g. an existing vehicle fire gate refused the request).
        return RESERVATIONS.predict(vehicle.getUUID(), tick, Math.max(80, gun.get(GunProp.SHOOT_DELAY_TIME) + 1));
    }

    public static void receive(MissileLaunchStateMessage message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        now(level);
        RESERVATIONS.synchronize(message.vehicle(), message.token(), message.pending());
    }
}
