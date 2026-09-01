package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.R73Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** R-73 渲染器：复用 superb 的 SBM 投射物渲染器（模型/贴图按实体 id 自动解析）。 */
public class R73Renderer extends BasicProjectileRenderer<R73Entity> {

    public R73Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
