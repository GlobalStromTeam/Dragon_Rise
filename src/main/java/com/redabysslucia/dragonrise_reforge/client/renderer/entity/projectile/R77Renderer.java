package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.R77Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** R-77 渲染器：复用 superb 的 SBM 投射物渲染器（模型/贴图按实体 id 自动解析）。 */
public class R77Renderer extends BasicProjectileRenderer<R77Entity> {

    public R77Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
