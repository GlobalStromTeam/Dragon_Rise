package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Aim9Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * AIM-9 渲染器：复用 superb 的 SBM 投射物渲染器（模型 + 尾焰 flare 特效）。
 */
public class Aim9Renderer extends BasicProjectileRenderer<Aim9Entity> {

    public Aim9Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
