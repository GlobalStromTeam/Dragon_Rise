package com.redabysslucia.dragonrise_reforge.utils;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.google.gson.annotations.SerializedName;

public class AirshipInfo extends EngineInfo {
    // 转向速度
    @SerializedName("SteeringSpeed")
    public float steeringSpeed = 0.1f;
    // 最大前进速度系数
    @SerializedName("MaxForwardSpeedRate")
    public float maxForwardSpeedRate = 0.2f;
    // 最大后退速度系数
    @SerializedName("MaxBackwardSpeedRate")
    public float maxBackwardSpeedRate = -0.1f;
    // 上升速度系数
    @SerializedName("LiftSpeedRate")
    public float liftSpeedRate = 0.2f;
    // 下降速度系数
    @SerializedName("SinkSpeedRate")
    public float sinkSpeedRate = -0.1f;
    // 水平阻力系数
    @SerializedName("DragHorizontal")
    public float dragHorizontal = 0.02f;
    // 垂直阻力系数
    @SerializedName("DragVertical")
    public float dragVertical = 0.01f;

    @Override
    public void work(VehicleEntity vehicle) {
        AirshipEngineUtils.airshipEngine(vehicle, this);
    }
}
