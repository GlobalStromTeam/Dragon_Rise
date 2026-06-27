package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;

import java.util.*;
import java.util.regex.Pattern;

public class BedrockVehicleModel extends BedrockModel {
    public static final Pattern WHEEL_PATTERN = Pattern.compile("^wheel(?<direction>[LR]).*$");
    public static final Pattern SHELL_PATTERN = Pattern.compile("^shell(?<id>\\d+)$");
    public static final Pattern TRACK_PATTERN = Pattern.compile("^track(?<type>Mov|Rot)(?<direction>[LR])(?<id>\\d+)$");
    public static final Pattern FLARE_PATTERN = Pattern.compile("^flare.*");
    public static final Pattern DOG_TAG_PATTERN = Pattern.compile("^.*_dogTag$");

    public List<BedrockBone> leftWheels;
    public List<BedrockBone> rightWheels;
    public List<BedrockBone> leftWheelsTurn;
    public List<BedrockBone> rightWheelsTurn;
    public List<BedrockBone> shell;
    public List<BedrockBone> leftTrackMove;
    public List<BedrockBone> leftTrackRot;
    public List<BedrockBone> rightTrackMove;
    public List<BedrockBone> rightTrackRot;
    public List<BedrockBone> flareBones;
    public List<BedrockBone> dogTagBones;

    public BedrockVehicleModel(BedrockModelPOJO pojo) {
        super(pojo);
        init();
    }

    public void init() {
        Map<String, BedrockBone> map = this.getBoneMap();

        List<BedrockBone> leftWheels = new ArrayList<>();
        List<BedrockBone> rightWheels = new ArrayList<>();
        List<BedrockBone> leftWheelsTurn = new ArrayList<>();
        List<BedrockBone> rightWheelsTurn = new ArrayList<>();

        Map<Integer, BedrockBone> tempShell = new HashMap<>();

        Map<Integer, BedrockBone> leftTrackMove = new HashMap<>();
        Map<Integer, BedrockBone> leftTrackRot = new HashMap<>();
        Map<Integer, BedrockBone> rightTrackMove = new HashMap<>();
        Map<Integer, BedrockBone> rightTrackRot = new HashMap<>();

        List<BedrockBone> flareBones = new ArrayList<>();
        List<BedrockBone> dogTagBones = new ArrayList<>();

        for (Map.Entry<String, BedrockBone> entry : map.entrySet()) {
            String name = entry.getKey();
            BedrockBone bone = entry.getValue();

            var matcher = WHEEL_PATTERN.matcher(name);
            if (matcher.matches()) {
                boolean left = "L".equals(matcher.group("direction"));
                boolean turn = name.endsWith("Turn");

                if (left) {
                    if (turn) {
                        leftWheelsTurn.add(bone);
                    } else {
                        leftWheels.add(bone);
                    }
                } else {
                    if (turn) {
                        rightWheelsTurn.add(bone);
                    } else {
                        rightWheels.add(bone);
                    }
                }
            }

            var matcherShell = SHELL_PATTERN.matcher(name);
            if (matcherShell.matches()) {
                int index = Integer.parseInt(matcherShell.group("id"));
                tempShell.put(index, bone);
            }

            var matcherTrackPart = TRACK_PATTERN.matcher(name);
            if (matcherTrackPart.matches()) {
                boolean isRot = "Rot".equals(matcherTrackPart.group("type"));
                boolean isL = "L".equals(matcherTrackPart.group("direction"));
                int index = Integer.parseInt(matcherTrackPart.group("id"));

                if (isRot) {
                    if (isL) {
                        leftTrackRot.put(index, bone);
                    } else {
                        rightTrackRot.put(index, bone);
                    }
                } else {
                    if (isL) {
                        leftTrackMove.put(index, bone);
                    } else {
                        rightTrackMove.put(index, bone);
                    }
                }
            }

            var matcherFlare = FLARE_PATTERN.matcher(name);
            if (matcherFlare.matches()) {
                flareBones.add(bone);
            }

            var matcherDogTag = DOG_TAG_PATTERN.matcher(name);
            if (matcherDogTag.matches()) {
                dogTagBones.add(bone);
            }
        }

        this.leftWheels = leftWheels;
        this.rightWheels = rightWheels;
        this.leftWheelsTurn = leftWheelsTurn;
        this.rightWheelsTurn = rightWheelsTurn;

        this.shell = new ArrayList<>(tempShell.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());

        this.leftTrackMove = new ArrayList<>(leftTrackMove.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
        this.leftTrackRot = new ArrayList<>(leftTrackRot.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
        this.rightTrackMove = new ArrayList<>(rightTrackMove.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());
        this.rightTrackRot = new ArrayList<>(rightTrackRot.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList());

        this.flareBones = flareBones;
        this.dogTagBones = dogTagBones;
    }
}