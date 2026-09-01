package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Gb250Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** GB-250 渲染器：复用 superb 的 SBM 投射物渲染器。 */
public class Gb250Renderer extends BasicProjectileRenderer<Gb250Entity> {

    public Gb250Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
