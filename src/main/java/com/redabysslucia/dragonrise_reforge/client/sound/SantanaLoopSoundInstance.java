package com.redabysslucia.dragonrise_reforge.client.sound;

import com.redabysslucia.dragonrise_reforge.entities.SantanaEntity;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * 桑塔纳「点灯」循环音：绑定车辆实体的客户端循环 SoundInstance。
 *
 * <p>写法参照 {@link R6DroneLoopSoundInstance}：{@code looping = true}，每 tick 跟随车辆位置；
 * 车辆被移除或开关关闭时由 {@link SantanaEntity} 侧 stop。
 */
public class SantanaLoopSoundInstance extends AbstractTickableSoundInstance {

    private final SantanaEntity vehicle;

    public SantanaLoopSoundInstance(SantanaEntity vehicle) {
        super(ModSounds.SANTANA_LOOP.get(), SoundSource.NEUTRAL, vehicle.level().getRandom());
        this.vehicle = vehicle;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0f;
        this.pitch = 1.0f;
        this.attenuation = Attenuation.LINEAR;
        this.x = vehicle.getX();
        this.y = vehicle.getY();
        this.z = vehicle.getZ();
    }

    @Override
    public void tick() {
        if (this.vehicle.isRemoved() || !this.vehicle.dr$isLightOn()) {
            this.stop();
            return;
        }
        this.x = this.vehicle.getX();
        this.y = this.vehicle.getY();
        this.z = this.vehicle.getZ();
    }
}
