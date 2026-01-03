package com.redabysslucia.dragonrise_reforge.utils.IK.Constraint;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * 平面约束 - 限制关节在平面内运动
 */
public class PlaneConstraint extends RotationConstraint {
    private final Vector3f planeNormal;

    public PlaneConstraint(Vector3fc planeNormal) {
        this.planeNormal = new Vector3f(planeNormal).normalize();
    }

    @Override
    public Vector3f apply(Vector3f desiredDirection,
                          Vector3fc parentPos, Vector3fc currentPos) {
        // 将目标方向投影到平面上
        Vector3f projected = new Vector3f(desiredDirection);

        // 移除法线方向的分量
        double dot = projected.dot(planeNormal);
        projected.sub(planeNormal.mul((float) dot, new Vector3f()));

        return projected.normalize();
    }
}
