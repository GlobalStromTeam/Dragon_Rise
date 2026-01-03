package com.redabysslucia.dragonrise_reforge.utils.IK.Constraint;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * 圆锥约束 - 限制关节在圆锥范围内旋转
 */
public class ConeConstraint extends RotationConstraint {
    private final Vector3f axis;
    private final double maxAngle; // 弧度

    public ConeConstraint(Vector3fc axis, double maxAngleDegrees) {
        this.axis = new Vector3f(axis).normalize();
        this.maxAngle = Math.toRadians(maxAngleDegrees);
    }

    @Override
    public Vector3f apply(Vector3f desiredDirection,
                          Vector3fc parentPos, Vector3fc currentPos) {
        Vector3f currentDir = new Vector3f(currentPos).sub(parentPos).normalize();
        Vector3f targetDir = new Vector3f(desiredDirection).normalize();

        // 计算当前方向与约束轴的角度
        double currentAngle = Math.acos(axis.dot(currentDir));

        // 计算目标方向与约束轴的角度
        double targetAngle = Math.acos(axis.dot(targetDir));

        // 如果目标角度超出限制，进行限制
        if (targetAngle > maxAngle) {
            // 计算旋转轴（当前方向与约束轴的叉积）
            Vector3f rotationAxis = new Vector3f(axis).cross(currentDir);

            if (rotationAxis.lengthSquared() < 1e-10) {
                // 如果平行，选择一个垂直轴
                rotationAxis = findPerpendicular(axis);
            }

            rotationAxis.normalize();

            // 计算需要旋转的角度
            double angleToRotate = maxAngle - currentAngle;

            // 创建旋转四元数
            Quaternionf rotation = new Quaternionf()
                    .fromAxisAngleRad(rotationAxis, (float) angleToRotate);

            // 旋转当前方向
            targetDir = rotation.transform(currentDir);
        }

        return targetDir.normalize();
    }
}
