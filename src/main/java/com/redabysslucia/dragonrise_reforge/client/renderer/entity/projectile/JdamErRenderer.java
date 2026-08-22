package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.JdamErEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** JDAM-ER 渲染器：复用 superb 的 SBM 投射物渲染器（含弹翼展开动画实例）。 */
public class JdamErRenderer extends BasicProjectileRenderer<JdamErEntity> {

    public JdamErRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
