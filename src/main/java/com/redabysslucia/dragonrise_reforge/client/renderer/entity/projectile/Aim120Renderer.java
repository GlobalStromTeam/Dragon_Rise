package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Aim120Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * AIM-120 渲染器：复用 superb 的 SBM 投射物渲染器（模型 + 尾焰 flare 特效）。
 * 模型/贴图按实体 id 自动解析：
 *   models/bedrock/projectile/aim120.geo.json
 *   textures/bedrock/projectile/aim120.png
 */
public class Aim120Renderer extends BasicProjectileRenderer<Aim120Entity> {

    public Aim120Renderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
