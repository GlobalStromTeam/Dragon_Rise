package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Ls6Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** LS-6 渲染器：复用 superb 的 SBM 投射物渲染器（含弹翼展开动画实例）。 */
public class Ls6Renderer extends BasicProjectileRenderer<Ls6Entity> {

    public Ls6Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
