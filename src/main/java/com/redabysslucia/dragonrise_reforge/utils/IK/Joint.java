package com.redabysslucia.dragonrise_reforge.utils.IK;

import com.redabysslucia.dragonrise_reforge.utils.IK.Constraint.IConstraint;
import com.redabysslucia.dragonrise_reforge.utils.IK.Constraint.OrientationConstraint;
import com.redabysslucia.dragonrise_reforge.utils.IK.Constraint.RotationConstraint;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public class Joint {
    private final Vector3f position;
    private final Vector3f restDirection; // 初始方向
    private final List<IConstraint> constraints;
    private Quaternionf localRotation;

    public Joint(Vector3fc position) {
        this.position = new Vector3f(position);
        this.restDirection = new Vector3f(0, 1, 0); // 默认Y轴方向
        this.constraints = new ArrayList<>();
        this.localRotation = new Quaternionf();
    }

    public Joint(Vector3fc position, Vector3fc restDirection) {
        this.position = new Vector3f(position);
        this.restDirection = new Vector3f(restDirection).normalize();
        this.constraints = new ArrayList<>();
        this.localRotation = new Quaternionf();
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Vector3f getRestDirection() {
        return new Vector3f(restDirection);
    }

    public void addConstraint(IConstraint constraint) {
        constraints.add(constraint);
    }

    public boolean hasConstraints() {
        return !constraints.isEmpty();
    }

    public boolean hasOrientationConstraint() {
        return constraints.stream()
                .anyMatch(c -> c instanceof OrientationConstraint);
    }

    public Vector3f applyConstraint(Vector3f desiredDirection,
                                    Vector3fc parentPos, Vector3fc currentPos) {
        Vector3f constrainedDir = new Vector3f(desiredDirection);

        for (IConstraint constraint : constraints) {
            if (constraint instanceof RotationConstraint) {
                constrainedDir = ((RotationConstraint) constraint)
                        .apply(constrainedDir, parentPos, currentPos);
            }
        }

        return constrainedDir;
    }

    public void applyOrientationConstraint(Quaternionfc targetOrientation,
                                           Vector3fc parentPos, Vector3fc currentPos) {
        for (IConstraint constraint : constraints) {
            if (constraint instanceof OrientationConstraint) {
                ((OrientationConstraint) constraint)
                        .apply(targetOrientation, parentPos, currentPos);
            }
        }
    }

    public Quaternionf getLocalRotation() {
        return new Quaternionf(localRotation);
    }

    public void setLocalRotation(Quaternionfc rotation) {
        this.localRotation.set(rotation);
    }
}
