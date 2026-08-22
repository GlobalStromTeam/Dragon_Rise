package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Pl12Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** PL-12 渲染器：复用 superb 的 SBM 投射物渲染器（模型/贴图按实体 id 自动解析）。 */
public class Pl12Renderer extends BasicProjectileRenderer<Pl12Entity> {

    public Pl12Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
