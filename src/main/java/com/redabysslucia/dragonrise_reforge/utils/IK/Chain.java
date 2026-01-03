package com.redabysslucia.dragonrise_reforge.utils.IK;

import com.redabysslucia.dragonrise_reforge.utils.IK.Constraint.IConstraint;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public class Chain {
    private final List<Joint> joints = new ArrayList<>();
    //private final List<Double> lengths = new ArrayList<>();

    public Chain addJoint(Vector3fc position) {
        Joint joint = new Joint(position);
        joints.add(joint);

//        if (joints.size() > 1) {
//            Vector3f prevPos = joints.get(joints.size() - 2).getPosition();
//            double length = prevPos.distance(position);
//            lengths.add(length);
//        }

        return this;
    }

    public Chain addJoint(Vector3fc position, Vector3fc restDirection) {
        Joint joint = new Joint(position, restDirection);
        joints.add(joint);

//        if (joints.size() > 1) {
//            Vector3f prevPos = joints.get(joints.size() - 2).getPosition();
//            double length = prevPos.distance(position);
//            lengths.add(length);
//        }

        return this;
    }

    public Chain addConstraint(int jointIndex, IConstraint constraint) {
        if (jointIndex >= 0 && jointIndex < joints.size()) {
            joints.get(jointIndex).addConstraint(constraint);
        }
        return this;
    }

    public FABRIK build(FABRIK.Config config) {
        return new FABRIK(joints, config);
    }
}
