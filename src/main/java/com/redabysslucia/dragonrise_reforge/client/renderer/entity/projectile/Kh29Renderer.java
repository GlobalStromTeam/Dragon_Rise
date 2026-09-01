package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Kh29Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Kh-29 渲染器：复用 superb 的 SBM 投射物渲染器（模型/贴图按实体 id 自动解析）。 */
public class Kh29Renderer extends BasicProjectileRenderer<Kh29Entity> {

    public Kh29Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
