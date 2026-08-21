package com.redabysslucia.dragonrise_reforge.client.sound;

import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * 侦察无人车移动循环音（参考 SBW VehicleSoundInstance$EngineSound）：
 * 客户端循环 SoundInstance（looping = true），绑定无人车实体，每 tick 跟随位置。
 * moving（普通移动）与 fast（冲刺）各创建一个实例，由 R6DroneClientHandler
 * 在遥控时 play、状态切换时 stop 旧的并 play 新的。
 */
public class R6DroneLoopSoundInstance extends AbstractTickableSoundInstance {

    private final R6DroneEntity drone;
    private final Minecraft client;

    /** true = fast（冲刺音效），false = moving（普通移动音效） */
    private final boolean fast;

    public R6DroneLoopSoundInstance(R6DroneEntity drone, Minecraft client, boolean fast) {
        super(fast ? ModSounds.R6_DRONE_FAST.get() : ModSounds.R6_DRONE_MOVING.get(),
                SoundSource.PLAYERS, client.level.getRandom());
        this.drone = drone;
        this.client = client;
        this.fast = fast;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.5f;
        this.pitch = 1.0f;
        this.x = drone.getX();
        this.y = drone.getY();
        this.z = drone.getZ();
    }

    @Override
    public void tick() {
        // 实体被移除 → 停止
        if (this.drone.isRemoved()) {
            this.stop();
            return;
        }
        // 跟随实体位置（音源在无人车处；遥控者相机在无人车上，距离 0 全音量）
        this.x = this.drone.getX();
        this.y = this.drone.getY();
        this.z = this.drone.getZ();
    }

    public boolean isFast() {
        return this.fast;
    }
}
