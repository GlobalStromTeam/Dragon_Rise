package com.redabyss.ralmod.RalModScript;

public class Rotation {
    private float timepassstart;
    public static float NowAngleToGoalAngle(float NowAngle, float GoalAngle){
        float dAngle;
        dAngle=(GoalAngle-NowAngle)*0.1f;
        return dAngle;
    }
}
