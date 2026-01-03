package com.redabysslucia.dragonrise_reforge.utils.IK.Constraint;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public abstract class RotationConstraint implements IConstraint {
    public abstract Vector3f apply(Vector3f desiredDirection, Vector3fc parentPos, Vector3fc currentPos);


    public Vector3f findPerpendicular(Vector3f axis) {
        Vector3f temp = new Vector3f(axis);

        // 尝试与(1,0,0)叉积
        Vector3f perpendicular = new Vector3f(1, 0, 0).cross(temp);
        if (perpendicular.lengthSquared() < 1e-10) {
            // 如果平行，尝试与(0,0,1)叉积
            perpendicular = new Vector3f(0, 0, 1).cross(temp);
        }

        return perpendicular.normalize();
    }
}
