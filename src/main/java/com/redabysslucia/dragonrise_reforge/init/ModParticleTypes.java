package com.redabysslucia.dragonrise_reforge.init;

import com.mojang.serialization.Codec;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.particle.SignalSmokeOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 信号棒彩色上升烟雾粒子类型注册（服务端/客户端共用 registry）。
 */
public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Dragonrise_reforge.MODID);

    public static final RegistryObject<ParticleType<SignalSmokeOption>> SIGNAL_SMOKE =
            REGISTRY.register("signal_smoke", () -> new ParticleType<>(true, SignalSmokeOption.DESERIALIZER) {
                @Override
                public Codec<SignalSmokeOption> codec() {
                    return SignalSmokeOption.CODEC;
                }
            });
}
