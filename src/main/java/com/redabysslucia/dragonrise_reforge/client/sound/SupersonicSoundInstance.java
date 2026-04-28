package com.redabysslucia.dragonrise_reforge.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class SupersonicSoundInstance extends AbstractTickableSoundInstance {

    private final Minecraft client;
    private final Entity entity;
    private double lastDistance;
    private int fade = 0;
    private boolean die = false;
    private boolean shouldPlay = false;

    public SupersonicSoundInstance(SoundEvent sound, Minecraft client, Entity entity) {
        super(sound, SoundSource.AMBIENT, entity.level().getRandom());
        this.client = client;
        this.entity = entity;
        this.looping = true;
        this.delay = 0;
    }

    public void setShouldPlay(boolean shouldPlay) {
        if (shouldPlay && !this.shouldPlay) {
            // 重新启用，重置状态
            this.die = false;
        }
        this.shouldPlay = shouldPlay;
    }

    public boolean isShouldPlay() {
        return shouldPlay;
    }

    @Override
    public void tick() {
        var player = this.client.player;
        if (entity.isRemoved() || player == null) {
            this.stop();
            return;
        }

        if (!shouldPlay) {
            this.die = true;
        } else {
            this.die = false;
        }

        if (this.die) {
            if (this.fade > 0) this.fade--;
            else if (this.fade == 0) {
                this.stop();
                return;
            }
        } else if (this.fade < 5) {
            this.fade++;
        }

        this.volume = 1.0f * fade;

        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();

        this.pitch = 1.0f;

        if (player.getVehicle() != this.entity) {
            double distance = this.entity.position().subtract(player.position()).length();
            this.pitch += (float) (0.16 * Math.atan(lastDistance - distance));
            this.lastDistance = distance;
        } else {
            this.lastDistance = 0;
        }
    }
}
