package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Gb500Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** GB-500 渲染器：复用 superb 的 SBM 投射物渲染器（模型/贴图按实体 id 自动解析）。 */
public class Gb500Renderer extends BasicProjectileRenderer<Gb500Entity> {

    public Gb500Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
