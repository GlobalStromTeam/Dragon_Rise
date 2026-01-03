package com.redabysslucia.dragonrise_reforge.utils.IK;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FABRIK {

    public static class Config {
        private double tolerance = 1e-5;
        private int maxIterations = 100;
        private boolean useConstraints = true;
        private boolean allowStretching = false;
        private double stretchingLimit = 1.5;

        public Config tolerance(double tolerance) {
            this.tolerance = tolerance;
            return this;
        }

        public Config maxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
            return this;
        }

        public Config useConstraints(boolean useConstraints) {
            this.useConstraints = useConstraints;
            return this;
        }

        public Config allowStretching(boolean allowStretching) {
            this.allowStretching = allowStretching;
            return this;
        }

        public Config stretchingLimit(double stretchingLimit) {
            this.stretchingLimit = stretchingLimit;
            return this;
        }
    }

    private final Config config;
    private final List<Joint> joints;
    private final Vector3f[] positions;
    private final double[] lengths;

    public FABRIK(List<Joint> joints, Config config) {
        this.joints = new ArrayList<>(joints);
        this.config = config;
        this.positions = new Vector3f[joints.size()];
        this.lengths = new double[joints.size() - 1];

        initialize();
    }

    private void initialize() {
        // 初始化位置数组
        for (int i = 0; i < joints.size(); i++) {
            positions[i] = new Vector3f(joints.get(i).getPosition());
        }

        // 计算段长度
        for (int i = 0; i < joints.size() - 1; i++) {
            lengths[i] = positions[i].distance(positions[i + 1]);
        }
    }


    /**
     * 求解逆运动学
     * @param target 目标位置
     * @return 是否成功收敛
     */
    public boolean solve(Vector3fc target) {
        return solve(target, null);
    }

    /**
     * 求解逆运动学（带目标朝向）
     * @param target 目标位置
     * @param targetOrientation 目标朝向（可为null）
     * @return 是否成功收敛
     */
    public boolean solve(Vector3fc target, Quaternionfc targetOrientation) {
        Vector3f basePosition = new Vector3f(positions[0]);
        double totalLength = Arrays.stream(lengths).sum();

        // 检查目标是否可到达
        if (!config.allowStretching) {
            double distanceToTarget = basePosition.distance(target);
            if (distanceToTarget > totalLength * config.stretchingLimit) {
                // 目标太远，直接伸展
                stretchToTarget(target);
                return false;
            }
        }

        // FABRIK主循环
        for (int iteration = 0; iteration < config.maxIterations; iteration++) {
            // 前向传递
            forwardPass(target, targetOrientation);

            // 后向传递
            backwardPass(basePosition);

            // 检查收敛
            if (checkConvergence(target)) {
                return true;
            }
        }

        return false;
    }

    private void forwardPass(Vector3fc target, Quaternionfc targetOrientation) {
        // 设置末端位置
        positions[positions.length - 1].set(target);

        // 从末端向根节点传递
        for (int i = positions.length - 2; i >= 0; i--) {
            Vector3f direction = new Vector3f();
            positions[i + 1].sub(positions[i], direction);

            // 应用约束
            if (config.useConstraints && i < joints.size() - 1) {
                Joint joint = joints.get(i + 1);
                direction = applyConstraints(direction, joint, i, false);
            }

            direction.normalize();
            direction.mul((float) lengths[i]);
            positions[i].set(positions[i + 1]).sub(direction);
        }

        // 应用末端效应器约束
        if (config.useConstraints && targetOrientation != null) {
            applyEndEffectorConstraint(targetOrientation);
        }
    }

    private void backwardPass(Vector3fc basePosition) {
        // 设置根节点位置
        positions[0].set(basePosition);

        // 从根节点向末端传递
        for (int i = 0; i < positions.length - 1; i++) {
            Vector3f direction = new Vector3f();
            positions[i + 1].sub(positions[i], direction);

            // 应用约束
            if (config.useConstraints) {
                Joint joint = joints.get(i + 1);
                direction = applyConstraints(direction, joint, i, true);
            }

            direction.normalize();
            direction.mul((float) lengths[i]);
            positions[i + 1].set(positions[i]).add(direction);
        }
    }

    private Vector3f applyConstraints(Vector3f direction, Joint joint, int segmentIndex,
                                      boolean isBackward) {
        if (!joint.hasConstraints()) {
            return direction;
        }

        Vector3f parentPos = isBackward ? positions[segmentIndex] : positions[segmentIndex + 1];
        Vector3f currentPos = isBackward ? positions[segmentIndex + 1] : positions[segmentIndex];

        return joint.applyConstraint(direction, parentPos, currentPos);
    }

    private void applyEndEffectorConstraint(Quaternionfc targetOrientation) {
        int lastIndex = positions.length - 1;
        if (lastIndex > 0) {
            Joint endJoint = joints.get(lastIndex);
            if (endJoint.hasOrientationConstraint()) {
                endJoint.applyOrientationConstraint(targetOrientation,
                        positions[lastIndex - 1], positions[lastIndex]);
            }
        }
    }

    private void stretchToTarget(Vector3fc target) {
        Vector3f direction = new Vector3f(target).sub(positions[0]);
        direction.normalize();

        for (int i = 1; i < positions.length; i++) {
            direction.mul((float) lengths[i - 1]);
            positions[i].set(positions[i - 1]).add(direction);
            direction.normalize();
        }
    }

    private boolean checkConvergence(Vector3fc target) {
        Vector3f endPos = positions[positions.length - 1];
        return endPos.distanceSquared(target) < config.tolerance * config.tolerance;
    }

    /**
     * 获取求解后的关节位置
     */
    public List<Vector3f> getJointPositions() {
        List<Vector3f> result = new ArrayList<>();
        for (Vector3f pos : positions) {
            result.add(new Vector3f(pos));
        }
        return result;
    }

    /**
     * 获取求解后的关节旋转
     */
    public List<Quaternionf> getJointRotations() {
        List<Quaternionf> rotations = new ArrayList<>();

        for (int i = 0; i < joints.size(); i++) {
            if (i == 0) {
                rotations.add(new Quaternionf());
                continue;
            }

            Vector3f parentPos = positions[i - 1];
            Vector3f currentPos = positions[i];
            Vector3f restDirection = joints.get(i).getRestDirection();

            Quaternionf rotation = calculateRotation(restDirection, parentPos, currentPos);
            rotations.add(rotation);
        }

        return rotations;
    }

    private Quaternionf calculateRotation(Vector3fc restDirection,
                                          Vector3fc parentPos, Vector3fc currentPos) {
        Vector3f currentDirection = new Vector3f(currentPos).sub(parentPos).normalize();
        Vector3f axis = new Vector3f(restDirection).cross(currentDirection);

        if (axis.lengthSquared() < 1e-10) {
            return new Quaternionf();
        }

        axis.normalize();
        double angle = Math.acos(restDirection.dot(currentDirection));

        return new Quaternionf()
                .fromAxisAngleRad(axis, (float) angle);
    }
}
