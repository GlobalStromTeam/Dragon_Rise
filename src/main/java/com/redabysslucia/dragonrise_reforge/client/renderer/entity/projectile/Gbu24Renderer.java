package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Gbu24Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** GBU-24 渲染器：复用 superb 的 SBM 投射物渲染器（含弹翼展开动画实例）。 */
public class Gbu24Renderer extends BasicProjectileRenderer<Gbu24Entity> {

    public Gbu24Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
