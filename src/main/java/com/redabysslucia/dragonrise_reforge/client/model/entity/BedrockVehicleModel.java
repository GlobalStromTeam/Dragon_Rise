package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BedrockVehicleModel extends BedrockModel {

    public static final Pattern WHEEL_PATTERN = Pattern.compile("^wheel(?<direction>[LR]).*$");
    public static final Pattern SHELL_PATTERN = Pattern.compile("^shell(?<id>\\d+)$");
    public static final Pattern TRACK_PATTERN = Pattern.compile("^track(?<type>Mov|Rot)(?<direction>[LR])(?<id>\\d+)$");
    public static final Pattern FLARE_PATTERN = Pattern.compile("^flare.*");
    public static final Pattern DOG_TAG_PATTERN = Pattern.compile("^.*_dogTag$");

    public List<BedrockBone> leftWheels = new ArrayList<>();
    public List<BedrockBone> rightWheels = new ArrayList<>();
    public List<BedrockBone> leftWheelsTurn = new ArrayList<>();
    public List<BedrockBone> rightWheelsTurn = new ArrayList<>();
    public List<BedrockBone> shell = new ArrayList<>();
    public List<BedrockBone> leftTrackMove = new ArrayList<>();
    public List<BedrockBone> leftTrackRot = new ArrayList<>();
    public List<BedrockBone> rightTrackMove = new ArrayList<>();
    public List<BedrockBone> rightTrackRot = new ArrayList<>();
    public List<BedrockBone> flareBones = new ArrayList<>();
    public List<BedrockBone> dogTagBones = new ArrayList<>();

    public BedrockVehicleModel(BedrockModelPOJO pojo) {
        super(pojo);
    }

    public void init() {
        Map<String, BedrockBone> map = getBoneMap();
        Map<Integer, BedrockBone> shellMap = new HashMap<>();
        Map<Integer, BedrockBone> leftTrackMoveMap = new HashMap<>();
        Map<Integer, BedrockBone> leftTrackRotMap = new HashMap<>();
        Map<Integer, BedrockBone> rightTrackMoveMap = new HashMap<>();
        Map<Integer, BedrockBone> rightTrackRotMap = new HashMap<>();

        for (Map.Entry<String, BedrockBone> entry : map.entrySet()) {
            String name = entry.getKey();
            BedrockBone bone = entry.getValue();

            var wheelMatcher = WHEEL_PATTERN.matcher(name);
            if (wheelMatcher.matches()) {
                boolean left = "L".equals(wheelMatcher.group("direction"));
                boolean turn = name.endsWith("Turn");
                if (left) {
                    if (turn) leftWheelsTurn.add(bone);
                    else leftWheels.add(bone);
                } else {
                    if (turn) rightWheelsTurn.add(bone);
                    else rightWheels.add(bone);
                }
            }

            var shellMatcher = SHELL_PATTERN.matcher(name);
            if (shellMatcher.matches()) {
                int index = Integer.parseInt(shellMatcher.group("id"));
                shellMap.put(index, bone);
            }

            var trackMatcher = TRACK_PATTERN.matcher(name);
            if (trackMatcher.matches()) {
                boolean isRot = "Rot".equals(trackMatcher.group("type"));
                boolean isL = "L".equals(trackMatcher.group("direction"));
                int index = Integer.parseInt(trackMatcher.group("id"));
                if (isRot) {
                    if (isL) leftTrackRotMap.put(index, bone);
                    else rightTrackRotMap.put(index, bone);
                } else {
                    if (isL) leftTrackMoveMap.put(index, bone);
                    else rightTrackMoveMap.put(index, bone);
                }
            }

            var flareMatcher = FLARE_PATTERN.matcher(name);
            if (flareMatcher.matches()) {
                flareBones.add(bone);
            }

            var dogTagMatcher = DOG_TAG_PATTERN.matcher(name);
            if (dogTagMatcher.matches()) {
                dogTagBones.add(bone);
            }
        }

        shell.addAll(shellMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
        leftTrackMove.addAll(leftTrackMoveMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
        leftTrackRot.addAll(leftTrackRotMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
        rightTrackMove.addAll(rightTrackMoveMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
        rightTrackRot.addAll(rightTrackRotMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
    }
}
