package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Ls6IrEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** LS-6 红外型渲染器：复用 superb 的 SBM 投射物渲染器（含弹翼展开动画实例）。 */
public class Ls6IrRenderer extends BasicProjectileRenderer<Ls6IrEntity> {

    public Ls6IrRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
