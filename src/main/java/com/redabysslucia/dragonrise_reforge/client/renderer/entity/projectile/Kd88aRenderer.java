package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Kd88aEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** KD-88A 渲染器：复用 superb 的 SBM 投射物渲染器（模型/贴图按实体 id 自动解析）。 */
public class Kd88aRenderer extends BasicProjectileRenderer<Kd88aEntity> {

    public Kd88aRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
