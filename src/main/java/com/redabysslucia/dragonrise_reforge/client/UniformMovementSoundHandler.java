package com.redabysslucia.dragonrise_reforge.client;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.sound.UniformAirborneTracker;
import com.redabysslucia.dragonrise_reforge.client.sound.UniformAirborneTracker.Action;
import com.redabysslucia.dragonrise_reforge.client.sound.UniformMovementCadence;
import com.redabysslucia.dragonrise_reforge.client.sound.UniformMovementCadence.Movement;
import com.redabysslucia.dragonrise_reforge.client.sound.UniformMovementSoundInstance;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

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
            // A wearer has one cadence even when both garments qualify.
            boolean equipped = isUniform(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST)
                    || isUniform(player.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS);
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
            boolean eligible = eligible(player, equipped && nearby);
            Action action = state.airborne.update(player.getX(), player.getY(), player.getZ(), player.onGround(), eligible);
            Movement requested = eligible && player.onGround() ?
                    player.isCrouching() ? Movement.CROUCH : player.isSprinting() ? Movement.SPRINT : Movement.WALK : Movement.IDLE;
            boolean due = state.cadence.update(player.getX(), player.getZ(), requested);
            state.sounds.removeIf(s -> s.isStopped() || (s.age() > 2 && !client.getSoundManager().isActive(s)));
            state.actionSounds.removeIf(s -> s.isStopped() || (s.age() > 2 && !client.getSoundManager().isActive(s)));
            state.landSounds.removeIf(s -> s.isStopped() || (s.age() > 2 && !client.getSoundManager().isActive(s)));
            if (!eligible) {
                state.actionSounds.forEach(UniformMovementSoundInstance::fadeOut);
                state.landSounds.forEach(UniformMovementSoundInstance::fadeOut);
            }
            Movement movement = state.cadence.movement();
            if (movement == Movement.IDLE) {
                state.sounds.forEach(UniformMovementSoundInstance::fadeOut);
                state.playingMovement = Movement.IDLE;
            } else if (due && state.cadence.movedThisTick() && action != Action.LAND) {
                state.play(client, movement);
                state.playingMovement = movement;
            }
            if (action != Action.NONE) state.playAction(client, action);
            if ((!equipped || !nearby) && state.sounds.isEmpty() && state.actionSounds.isEmpty()
                    && state.landSounds.isEmpty()) WEARERS.remove(id);
        }
        WEARERS.entrySet().removeIf(entry -> {
            if (seen.contains(entry.getKey())) return false;
            entry.getValue().stop(client);
            return true;
        });
    }

    private static boolean isUniform(ItemStack stack, EquipmentSlot slot) {
        if (!(stack.getItem() instanceof ArmorItem armor) || armor.getEquipmentSlot() != slot) return false;
        var id = ForgeRegistries.ITEMS.getKey(armor);
        return id != null && Dragonrise_reforge.MODID.equals(id.getNamespace());
    }

    private static boolean eligible(Player player, boolean equippedAndNearby) {
        return equippedAndNearby && player.isAlive() && !player.isRemoved() && !player.isSpectator() && !player.isSilent()
                && !player.isPassenger() && !player.isInWaterOrBubble() && !player.isInLava()
                && !player.isSwimming() && !player.isFallFlying() && !player.getAbilities().flying
                && (player.getPose() == Pose.STANDING || player.getPose() == Pose.CROUCHING);
    }

    private static final class Wearer {
        // Crouch retains its separately tuned cadence; normal gaits use vanilla step distance.
        private static final double[] WALK_FRAGMENT_TICKS = {13.5, 14.0, 13.94, 14.05351474, 14.11882086, 13.37868481};
        private final Player player;
        private final UniformMovementCadence cadence = new UniformMovementCadence();
        private final UniformAirborneTracker airborne = new UniformAirborneTracker();
        private final ArrayDeque<UniformMovementSoundInstance> sounds = new ArrayDeque<>();
        private final ArrayDeque<UniformMovementSoundInstance> actionSounds = new ArrayDeque<>();
        /** Landing and takeoff rotate independently so each pool keeps its own "never repeat" history. */
        private final ArrayDeque<UniformMovementSoundInstance> landSounds = new ArrayDeque<>();
        private int lastWalk = -1;
        private int lastSprint = -1;
        private int lastLand = -1;
        private Movement playingMovement = Movement.IDLE;

        private Wearer(Player player) {
            this.player = player;
        }

        private void play(Minecraft client, Movement movement) {
            boolean sprint = movement == Movement.SPRINT;
            var events = sprint ? ModSounds.UNIFORM_GEAR_SPRINT : ModSounds.UNIFORM_GEAR_WALK;
            int count = events.size();
            int previous = sprint ? lastSprint : lastWalk;
            int variant = previous < 0 ? player.getRandom().nextInt(count) :
                    (previous + 1 + player.getRandom().nextInt(count - 1)) % count;
            if (sprint) lastSprint = variant;
            else lastWalk = variant;
            // Same-state fragments overlap at full length; only state changes crossfade.
            if (playingMovement != movement) sounds.forEach(UniformMovementSoundInstance::fadeOut);
            if (movement == Movement.CROUCH) cadence.fragmentDurationTicks(WALK_FRAGMENT_TICKS[variant]);
            var event = events.get(variant).get();
            UniformMovementSoundInstance sound = new UniformMovementSoundInstance(player, event, movement == Movement.CROUCH);
            sounds.addLast(sound);
            client.getSoundManager().play(sound);
        }

        /** One-shot transition sound: jump uses the walk pool, landing uses its own two BodyFallDown samples. */
        private void playAction(Minecraft client, Action action) {
            boolean landing = action == Action.LAND;
            var events = landing ? ModSounds.UNIFORM_GEAR_LAND : ModSounds.UNIFORM_GEAR_WALK;
            int count = events.size();
            int previous = landing ? lastLand : lastWalk;
            int variant = previous < 0 ? player.getRandom().nextInt(count) :
                    (previous + 1 + player.getRandom().nextInt(count - 1)) % count;
            if (landing) lastLand = variant;
            else lastWalk = variant;
            var event = events.get(variant).get();
            // Landing keeps the crouch attenuation it was already heard with while airborne; jump stays full volume.
            UniformMovementSoundInstance sound = new UniformMovementSoundInstance(player, event, landing && player.isCrouching());
            (landing ? landSounds : actionSounds).addLast(sound);
            client.getSoundManager().play(sound);
        }

        private void stop(Minecraft client) {
            sounds.forEach(s -> client.getSoundManager().stop(s));
            actionSounds.forEach(s -> client.getSoundManager().stop(s));
            landSounds.forEach(s -> client.getSoundManager().stop(s));
            sounds.clear();
            actionSounds.clear();
            landSounds.clear();
        }
    }
}
