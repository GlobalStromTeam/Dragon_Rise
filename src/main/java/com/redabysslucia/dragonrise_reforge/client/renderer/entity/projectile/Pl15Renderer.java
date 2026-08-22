package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Pl15Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** PL-15 渲染器：复用 superb 的 SBM 投射物渲染器（模型/贴图按实体 id 自动解析）。 */
public class Pl15Renderer extends BasicProjectileRenderer<Pl15Entity> {

    public Pl15Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
