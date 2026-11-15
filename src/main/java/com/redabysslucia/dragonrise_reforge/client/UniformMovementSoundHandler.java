package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.sound.UniformMovementCadence;
import com.redabysslucia.dragonrise_reforge.client.sound.UniformMovementCadence.Movement;
import com.redabysslucia.dragonrise_reforge.client.sound.UniformMovementSoundInstance;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Client-local observation also covers nearby wearers through vanilla player/equipment synchronization. */
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class UniformMovementSoundHandler {
    private static final double AUDIBLE_DISTANCE_SQUARED = 8 * 8;
    private static final Map<UUID, Wearer> WEARERS = new HashMap<>();
    private static ClientLevel lastLevel;

    private UniformMovementSoundHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level != lastLevel || client.player == null) {
            WEARERS.values().forEach(w -> w.stop(client));
            WEARERS.clear();
            lastLevel = client.level;
        }
        if (client.level == null || client.player == null || client.isPaused()) return;
        Entity listener = client.getCameraEntity();
        if (listener == null) listener = client.player;
        Set<UUID> seen = new HashSet<>();
        for (Player player : client.level.players()) {
            UUID id = player.getUUID();
            boolean equipped = player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RUSSIAN_SOLDIER_CHEST.get());
            boolean nearby = listener.distanceToSqr(player) <= AUDIBLE_DISTANCE_SQUARED;
            Wearer state = WEARERS.get(id);
            if (state != null && state.player != player) {
                state.stop(client);
                WEARERS.remove(id);
                state = null;
            }
            if (state == null) {
                if (!equipped || !nearby) continue;
                state = new Wearer(player);
                WEARERS.put(id, state);
            }
            seen.add(id);
            Movement requested = eligible(player, equipped && nearby) ?
                    player.isCrouching() ? Movement.CROUCH : player.isSprinting() ? Movement.SPRINT : Movement.WALK : Movement.IDLE;
            boolean due = state.cadence.update(player.getX(), player.getZ(), requested);
            state.sounds.removeIf(s -> s.isStopped() || (s.age() > 2 && !client.getSoundManager().isActive(s)));
            Movement movement = state.cadence.movement();
            if (movement == Movement.IDLE) {
                state.sounds.forEach(UniformMovementSoundInstance::fadeOut);
                state.playingMovement = Movement.IDLE;
            } else if (due && state.cadence.movedThisTick()) {
                state.play(client, movement);
                state.playingMovement = movement;
            }
            if ((!equipped || !nearby) && state.sounds.isEmpty()) WEARERS.remove(id);
        }
        WEARERS.entrySet().removeIf(entry -> {
            if (seen.contains(entry.getKey())) return false;
            entry.getValue().stop(client);
            return true;
        });
    }

    private static boolean eligible(Player player, boolean equippedAndNearby) {
        return equippedAndNearby && player.isAlive() && !player.isRemoved() && !player.isSpectator() && !player.isSilent()
                && player.onGround() && !player.isPassenger() && !player.isInWaterOrBubble() && !player.isInLava()
                && !player.isSwimming() && !player.isFallFlying() && !player.getAbilities().flying
                && (player.getPose() == Pose.STANDING || player.getPose() == Pose.CROUCHING);
    }

    private static final class Wearer {
        // Full OGG durations in 20 Hz ticks; only the trigger clock is accelerated.
        private static final double[] WALK_FRAGMENT_TICKS = {13.5, 14.0, 13.94};
        private static final double[] SPRINT_FRAGMENT_TICKS = {6.6, 6.8, 11.4};
        private final Player player;
        private final UniformMovementCadence cadence = new UniformMovementCadence();
        private final ArrayDeque<UniformMovementSoundInstance> sounds = new ArrayDeque<>();
        private int lastWalk = -1;
        private int lastSprint = -1;
        private Movement playingMovement = Movement.IDLE;

        private Wearer(Player player) {
            this.player = player;
        }

        private void play(Minecraft client, Movement movement) {
            boolean sprint = movement == Movement.SPRINT;
            int previous = sprint ? lastSprint : lastWalk;
            int variant = previous < 0 ? player.getRandom().nextInt(3) :
                    (previous + 1 + player.getRandom().nextInt(2)) % 3;
            if (sprint) lastSprint = variant;
            else lastWalk = variant;
            // Same-state fragments overlap at full length; only state changes crossfade.
            if (playingMovement != movement) sounds.forEach(UniformMovementSoundInstance::fadeOut);
            cadence.fragmentDurationTicks((sprint ? SPRINT_FRAGMENT_TICKS : WALK_FRAGMENT_TICKS)[variant]);
            var event = (sprint ? ModSounds.UNIFORM_GEAR_SPRINT : ModSounds.UNIFORM_GEAR_WALK).get(variant).get();
            UniformMovementSoundInstance sound = new UniformMovementSoundInstance(player, event, movement == Movement.CROUCH);
            sounds.addLast(sound);
            client.getSoundManager().play(sound);
        }

        private void stop(Minecraft client) {
            sounds.forEach(s -> client.getSoundManager().stop(s));
            sounds.clear();
        }
    }
}
