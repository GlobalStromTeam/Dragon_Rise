package com.redabysslucia.dragonrise_reforge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 信号棒彩色上升烟雾粒子：贴图用原版 POOF 的 generic 帧（particles/signal_smoke.json），
 * 颜色来自 SignalSmokeOption(RGB)。运动只向上为主 + 少量横向扩散：
 * 粒子缓慢膨胀、寿命约 50-80 tick，末尾渐隐。半透明渲染。
 */
public class SignalSmokeParticle extends TextureSheetParticle {

    private final SpriteSet sprites;
    private float baseScale;

    protected SignalSmokeParticle(ClientLevel level, double x, double y, double z,
                                  double xd, double yd, double zd,
                                  float r, float g, float b, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        // 少量横向扩散：给一点随机 x/z 初速，让烟柱自然蓬松但不过度弥漫
        this.xd = (this.random.nextDouble() - 0.5) * 0.06;
        this.yd = Math.abs(yd) > 0.01 ? yd * 0.8 : 0.25; // 向上飘（保险默认值）
        this.zd = (this.random.nextDouble() - 0.5) * 0.06;
        this.gravity = 0.0f;
        this.lifetime = 100 + this.random.nextInt(60); // 100~160 tick（烟柱更高更久、远处可见）
        this.quadSize = 0.35f + this.random.nextFloat() * 0.3f; // 小尺寸（用户要求）
        this.baseScale = this.quadSize;
        this.setSpriteFromAge(sprites);
        // 高不透明度彩色烟（纯色不掺白，远处醒目）
        this.setAlpha(0.85f);
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        // 持续上飘；横向速度缓慢衰减（只保留少量扩散，不无限弥漫）
        this.yd += 0.004;
        this.xd *= 0.96;
        this.zd *= 0.96;
        this.move(this.xd, this.yd, this.zd);
        // 缓慢膨胀
        this.quadSize = this.baseScale * (1.0f + this.age * 0.02f);
        this.setSpriteFromAge(this.sprites);
        // 末尾渐隐
        if (this.age > this.lifetime - 20) {
            this.setAlpha(this.alpha * 0.94f);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SignalSmokeOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SignalSmokeOption type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new SignalSmokeParticle(level, x, y, z, xd, yd, zd,
                    type.getRed(), type.getGreen(), type.getBlue(), this.sprites);
        }
    }
}
