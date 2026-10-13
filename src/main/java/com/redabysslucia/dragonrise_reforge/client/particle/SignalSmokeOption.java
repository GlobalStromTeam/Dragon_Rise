package com.redabysslucia.dragonrise_reforge.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redabysslucia.dragonrise_reforge.init.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 信号棒彩色烟雾粒子参数：携带 RGB。
 *
 * <p>1.21.1 迁移：{@code ParticleOptions.Deserializer} 已被移除
 * （1.20.1 时代的 {@code fromNetwork}/{@code fromCommand} 写法不再存在）。
 * 现在序列化能力挂在 {@link ParticleType} 上，由两部分组成：
 * <ul>
 *   <li>{@link #CODEC}（{@link MapCodec}）—— 数据包/命令序列化</li>
 *   <li>{@link #STREAM_CODEC}（{@link StreamCodec}）—— 网络同步</li>
 * </ul>
 * 两者在 {@link ModParticleTypes#SIGNAL_SMOKE} 注册时交给 {@code ParticleType}。
 * 本类因此不再需要 {@code writeToNetwork}/{@code writeToString}。
 */
public class SignalSmokeOption implements ParticleOptions {

    /** 数据包/命令用（MapCodec）。 */
    public static final MapCodec<SignalSmokeOption> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.fieldOf("r").forGetter(o -> o.red),
            Codec.FLOAT.fieldOf("g").forGetter(o -> o.green),
            Codec.FLOAT.fieldOf("b").forGetter(o -> o.blue)
    ).apply(inst, SignalSmokeOption::new));

    /** 网络同步用。 */
    public static final StreamCodec<RegistryFriendlyByteBuf, SignalSmokeOption> STREAM_CODEC =
            StreamCodec.of(
                    (buf, option) -> {
                        buf.writeFloat(option.red);
                        buf.writeFloat(option.green);
                        buf.writeFloat(option.blue);
                    },
                    buf -> new SignalSmokeOption(buf.readFloat(), buf.readFloat(), buf.readFloat()));

    private final float red;
    private final float green;
    private final float blue;

    public SignalSmokeOption(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public float getRed() {
        return this.red;
    }

    public float getGreen() {
        return this.green;
    }

    public float getBlue() {
        return this.blue;
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticleTypes.SIGNAL_SMOKE.get();
    }
}
