package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * 双尾蝎渲染器：直接沿用 SBW 的载具渲染，不做任何遮挡特例。
 * <p>
 * 早先版本在本机玩家遥控时会把整机藏掉 —— 因为 SBW 的遥控相机把视点放在无人机原点前 0.18 格，
 * 对巴掌大的四轴没问题，对机身长约 11.5 格的双尾蝎就落在机身内部，画出来只会糊住视野。
 * 现在遥控相机已改为<b>座位机位</b>（机头光电球等，均在机身外形之外，见
 * {@code RemoteAircraftCameraMixin}），模型可以正常渲染，因此移除该特例。
 */
public class ShuangweixieRenderer extends GeoVehicleRenderer<ShuangweixieEntity> {

    public ShuangweixieRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }
}
