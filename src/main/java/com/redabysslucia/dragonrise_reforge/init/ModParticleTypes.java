package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.particle.SignalSmokeOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 信号棒彩色上升烟雾粒子类型注册（服务端/客户端共用 registry）。
 *
 * <p>1.21.1 迁移要点：
 * <ul>
 *   <li>粒子类型挂在原版 {@link BuiltInRegistries#PARTICLE_TYPE} 下
 *       （Forge 时代的 {@code ForgeRegistries.PARTICLE_TYPES} 已不存在）。</li>
 *   <li>{@link ParticleType} 的构造签名变为 {@code (boolean overrideLimiter, MapCodec, StreamCodec)}，
 *       1.20.1 的 {@code ParticleOptions.Deserializer} 已被移除。</li>
 * </ul>
 */
public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTRY =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Dragonrise_reforge.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SignalSmokeOption>> SIGNAL_SMOKE =
            REGISTRY.register("signal_smoke", () -> new ParticleType<>(true) {
                @Override
                public com.mojang.serialization.MapCodec<SignalSmokeOption> codec() {
                    return SignalSmokeOption.CODEC;
                }

                @Override
                public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, SignalSmokeOption> streamCodec() {
                    return SignalSmokeOption.STREAM_CODEC;
                }
            });
}
