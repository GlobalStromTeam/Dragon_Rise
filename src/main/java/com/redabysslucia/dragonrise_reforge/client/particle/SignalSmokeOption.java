package com.redabysslucia.dragonrise_reforge.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * 信号棒彩色烟雾粒子参数：携带 RGB（0-1）。网络经 FriendlyByteBuf 传 3 float。
 */
public class SignalSmokeOption implements ParticleOptions {

    public static final Codec<SignalSmokeOption> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("r").forGetter(o -> o.red),
            Codec.FLOAT.fieldOf("g").forGetter(o -> o.green),
            Codec.FLOAT.fieldOf("b").forGetter(o -> o.blue)
    ).apply(inst, SignalSmokeOption::new));

    public static final Deserializer<SignalSmokeOption> DESERIALIZER = new Deserializer<>() {
        @Override
        public SignalSmokeOption fromNetwork(ParticleType<SignalSmokeOption> type, FriendlyByteBuf buf) {
            return new SignalSmokeOption(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public SignalSmokeOption fromCommand(ParticleType<SignalSmokeOption> type,
                                             com.mojang.brigadier.StringReader reader)
                throws com.mojang.brigadier.exceptions.CommandSyntaxException {
            reader.skipWhitespace();
            float r = reader.readFloat();
            reader.skipWhitespace();
            float g = reader.readFloat();
            reader.skipWhitespace();
            float b = reader.readFloat();
            return new SignalSmokeOption(r, g, b);
        }
    };

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
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(this.red);
        buf.writeFloat(this.green);
        buf.writeFloat(this.blue);
    }

    @Override
    public String writeToString() {
        ResourceLocation key = com.redabysslucia.dragonrise_reforge.init.ModParticleTypes.SIGNAL_SMOKE.getId();
        return key == null ? "signal_smoke" : key.toString();
    }

    @Override
    public ParticleType<?> getType() {
        return com.redabysslucia.dragonrise_reforge.init.ModParticleTypes.SIGNAL_SMOKE.get();
    }
}
