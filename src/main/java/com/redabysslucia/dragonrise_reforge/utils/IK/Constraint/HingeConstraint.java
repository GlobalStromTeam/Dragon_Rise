package com.redabysslucia.dragonrise_reforge.utils.IK.Constraint;

import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * 铰链约束 - 限制关节绕特定轴旋转
 */
public class HingeConstraint extends RotationConstraint {
    private final Vector3f hingeAxis;
    private final double minAngle; // 弧度
    private final double maxAngle; // 弧度

    public HingeConstraint(Vector3fc hingeAxis, double minAngleDegrees, double maxAngleDegrees) {
        this.hingeAxis = new Vector3f(hingeAxis).normalize();
        this.minAngle = Math.toRadians(minAngleDegrees);
        this.maxAngle = Math.toRadians(maxAngleDegrees);
    }

    @Override
    public Vector3f apply(Vector3f desiredDirection,
                          Vector3fc parentPos, Vector3fc currentPos) {
        Vector3f currentDir = new Vector3f(currentPos).sub(parentPos).normalize();
        Vector3f targetDir = new Vector3f(desiredDirection).normalize();

        // 计算当前方向在铰链平面上的投影
        Vector3f currentInPlane = removeComponent(currentDir, hingeAxis);
        Vector3f targetInPlane = removeComponent(targetDir, hingeAxis);

        if (currentInPlane.lengthSquared() < 1e-10 ||
                targetInPlane.lengthSquared() < 1e-10) {
            return currentDir; // 保持原方向
        }

        currentInPlane.normalize();
        targetInPlane.normalize();

        // 计算当前角度和目标角度
        double currentAngle = calculateAngle(currentInPlane);
        double targetAngle = calculateAngle(targetInPlane);

        // 限制角度范围
        double clampedAngle = Mth.clamp(targetAngle, minAngle, maxAngle);

        // 计算旋转后的方向
        Quaternionf rotation = new Quaternionf()
                .fromAxisAngleRad(hingeAxis, (float) (clampedAngle - currentAngle));

        return rotation.transform(currentDir).normalize();
    }

    private Vector3f removeComponent(Vector3f vector, Vector3fc component) {
        double dot = vector.dot(component);
        return new Vector3f(vector).sub(new Vector3f(component).mul((float) dot));
    }

    private double calculateAngle(Vector3f vector) {
        // 使用参考向量计算角度
        Vector3f reference = findPerpendicular(hingeAxis);
        double dot = reference.dot(vector);
        double cross = new Vector3f(reference).cross(vector).dot(hingeAxis);

        return Math.atan2(cross, dot);
    }
}
