package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Gbu12Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * GBU-12 渲染器：复用 superb 的 SBM 投射物渲染器。
 */
public class Gbu12Renderer extends BasicProjectileRenderer<Gbu12Entity> {

    public Gbu12Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
