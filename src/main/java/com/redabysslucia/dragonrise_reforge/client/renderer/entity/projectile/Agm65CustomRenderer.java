package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Agm65CustomEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * 自研 AGM-65 渲染器：复用 superb 的 SBM 投射物渲染器（模型 + 尾焰 flare 特效）。
 */
public class Agm65CustomRenderer extends BasicProjectileRenderer<Agm65CustomEntity> {

    public Agm65CustomRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
