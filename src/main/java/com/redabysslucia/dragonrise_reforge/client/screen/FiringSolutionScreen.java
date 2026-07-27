package com.redabysslucia.dragonrise_reforge.client.screen;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase;
import com.redabysslucia.dragonrise_reforge.firecontrol.FireControlComputation;
import com.redabysslucia.dragonrise_reforge.firecontrol.FireControlSolution;
import com.redabysslucia.dragonrise_reforge.firecontrol.FireControlStatus;
import com.redabysslucia.dragonrise_reforge.firecontrol.IndirectFireBallistics;
import com.redabysslucia.dragonrise_reforge.firecontrol.TrajectoryMode;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.redabysslucia.dragonrise_reforge.network.message.SetFireControlMessage;
import com.redabysslucia.dragonrise_reforge.network.message.ToggleTakeoverMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class FiringSolutionScreen extends Screen {

    private final IndirectFireVehicleBase vehicle;
    private final Player player;

    private EditBox targetXField;
    private EditBox targetYField;
    private EditBox targetZField;
    private Button applyButton;
    private Button clearButton;
    private Button closeButton;
    private Button takeoverToggle;

    private FireControlComputation preview;
    private TrajectoryMode trajectoryMode = TrajectoryMode.LOW;

    private static final int PANEL_WIDTH = 210;
    private static final int PANEL_HEIGHT = 200;
    /** 默认目标水平距离（格） */
    private static final double DEFAULT_TARGET_RANGE = 120.0;

    public FiringSolutionScreen(VehicleEntity vehicle, Player player) {
        super(Component.translatable("screen.dragonrise_reforge.fire_control.title"));
        this.vehicle = (IndirectFireVehicleBase) vehicle;
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        Font font = this.minecraft.font;

        int labelWidth = 8;
        int fieldWidth = 50;
        int fieldY = panelY + 40;
        int fieldHeight = 12;
        int xCol1 = panelX + 10 + labelWidth;

        targetXField = new EditBox(font, xCol1, fieldY, fieldWidth, fieldHeight, Component.literal("X"));
        targetYField = new EditBox(font, xCol1 + 60, fieldY, fieldWidth, fieldHeight, Component.literal("Y"));
        targetZField = new EditBox(font, xCol1 + 120, fieldY, fieldWidth, fieldHeight, Component.literal("Z"));

        BlockPos defaultTarget = defaultTargetPos();
        targetXField.setValue(String.valueOf(defaultTarget.getX()));
        targetYField.setValue(String.valueOf(defaultTarget.getY()));
        targetZField.setValue(String.valueOf(defaultTarget.getZ()));

        targetXField.setFilter(s -> s.isEmpty() || s.equals("-") || s.matches("-?\\d{0,7}"));
        targetYField.setFilter(s -> s.isEmpty() || s.equals("-") || s.matches("-?\\d{0,7}"));
        targetZField.setFilter(s -> s.isEmpty() || s.equals("-") || s.matches("-?\\d{0,7}"));

        addRenderableWidget(targetXField);
        addRenderableWidget(targetYField);
        addRenderableWidget(targetZField);

        int buttonY = panelY + PANEL_HEIGHT - 48;
        applyButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.dragonrise_reforge.fire_control.apply"),
                button -> applySolution()
        ).bounds(panelX + 10, buttonY, 55, 14).build());

        clearButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.dragonrise_reforge.fire_control.clear"),
                button -> clearSolution()
        ).bounds(panelX + 70, buttonY, 55, 14).build());

        closeButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.dragonrise_reforge.fire_control.close"),
                button -> onClose()
        ).bounds(panelX + 130, buttonY, 55, 14).build());

        int takeoverY = panelY + PANEL_HEIGHT - 28;
        takeoverToggle = addRenderableWidget(Button.builder(
                Component.literal(""),
                button -> toggleTakeover()
        ).bounds(panelX + 10, takeoverY, PANEL_WIDTH - 20, 14).build());
        updateTakeoverLabel();

        updatePreview();
    }

    /**
     * 默认目标：车体水平前方约 120 格，高度取车体 Y。
     */
    private BlockPos defaultTargetPos() {
        if (vehicle.isFireControlActive()) {
            BlockPos saved = vehicle.getFireControlTarget();
            if (!saved.equals(BlockPos.ZERO)) {
                return saved;
            }
        }
        Vec3 origin = vehicle.position();
        int seat = vehicle.getTurretControllerIndex();
        if (seat >= 0) {
            try {
                Vec3 muzzle = vehicle.getShootPos(seat, 1.0f);
                if (muzzle != null) {
                    origin = new Vec3(muzzle.x, vehicle.getY(), muzzle.z);
                }
            } catch (Exception ignored) {
            }
        }
        Vec3 forward = vehicle.getForward();
        double horiz = Math.sqrt(forward.x * forward.x + forward.z * forward.z);
        if (horiz < 1.0E-4) {
            float yRot = vehicle.getYRot() * Mth.DEG_TO_RAD;
            forward = new Vec3(-Math.sin(yRot), 0, Math.cos(yRot));
            horiz = 1.0;
        }
        double nx = forward.x / horiz;
        double nz = forward.z / horiz;
        double tx = origin.x + nx * DEFAULT_TARGET_RANGE;
        double tz = origin.z + nz * DEFAULT_TARGET_RANGE;
        return BlockPos.containing(tx, vehicle.getY(), tz);
    }

    private void applySolution() {
        if (!vehicle.isMainCannonSelected()) {
            player.displayClientMessage(Component.translatable("screen.dragonrise_reforge.fire_control.weapon_warning")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        if (preview == null || !preview.isSuccess()) {
            return;
        }
        try {
            int x = Integer.parseInt(targetXField.getValue());
            int y = Integer.parseInt(targetYField.getValue());
            int z = Integer.parseInt(targetZField.getValue());
            BlockPos target = new BlockPos(x, y, z);
            // 不使用散布：radius 固定 0
            ModNetwork.PACKET_HANDLER.sendToServer(SetFireControlMessage.apply(
                    vehicle.getId(), target, 0, trajectoryMode, true
            ));
        } catch (NumberFormatException ignored) {
            player.displayClientMessage(Component.translatable("message.dragonrise_reforge.fire_control.invalid_input")
                    .withStyle(ChatFormatting.RED), true);
        }
    }

    private void clearSolution() {
        ModNetwork.PACKET_HANDLER.sendToServer(SetFireControlMessage.clear(vehicle.getId()));
    }

    private void toggleTakeover() {
        if (!vehicle.isFireControlActive()) {
            player.displayClientMessage(Component.translatable(
                    "message.dragonrise_reforge.fire_control.takeover_requires_active"
            ).withStyle(ChatFormatting.YELLOW), true);
            return;
        }
        boolean newState = !vehicle.isFireControlTakeoverEnabled();
        ModNetwork.PACKET_HANDLER.sendToServer(new ToggleTakeoverMessage(vehicle.getId(), newState));
        updateTakeoverLabel();
    }

    private void updateTakeoverLabel() {
        if (takeoverToggle == null) return;
        boolean enabled = vehicle.isFireControlActive() && vehicle.isFireControlTakeoverEnabled();
        takeoverToggle.setMessage(Component.translatable("screen.dragonrise_reforge.fire_control.takeover")
                .append(": ")
                .append(Component.translatable(enabled
                                ? "screen.dragonrise_reforge.fire_control.takeover.on"
                                : "screen.dragonrise_reforge.fire_control.takeover.off")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY)));
    }

    private void updatePreview() {
        try {
            int x = Integer.parseInt(targetXField.getValue());
            int y = Integer.parseInt(targetYField.getValue());
            int z = Integer.parseInt(targetZField.getValue());
            BlockPos target = new BlockPos(x, y, z);
            preview = IndirectFireBallistics.solve(
                    vehicle, vehicle.getTurretControllerIndex(), target, trajectoryMode
            );
        } catch (NumberFormatException e) {
            preview = FireControlComputation.failure(FireControlStatus.INVALID_INPUT);
        }

        if (applyButton != null) {
            boolean mainCannonReady = vehicle.isMainCannonSelected();
            applyButton.active = preview != null && preview.isSuccess() && mainCannonReady;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (player.getVehicle() != vehicle) {
            onClose();
            return;
        }
        updatePreview();
        updateTakeoverLabel();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xCC000000);
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, 0xFFFFC700);
        graphics.fill(panelX, panelY + PANEL_HEIGHT - 1, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFFFC700);
        graphics.fill(panelX, panelY, panelX + 1, panelY + PANEL_HEIGHT, 0xFFFFC700);
        graphics.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFFFFC700);

        Font font = this.minecraft.font;
        int textX = panelX + 10;

        graphics.drawString(font, Component.translatable("screen.dragonrise_reforge.fire_control.title"),
                textX, panelY + 8, 0xFFFFC700, false);

        int labelY = panelY + 40;
        graphics.drawString(font, "X:", textX, labelY + 2, 0xFFFFFFFF, false);
        graphics.drawString(font, "Y:", textX + 60, labelY + 2, 0xFFFFFFFF, false);
        graphics.drawString(font, "Z:", textX + 120, labelY + 2, 0xFFFFFFFF, false);

        int resultY = panelY + 70;
        if (preview != null && preview.isSuccess()) {
            FireControlSolution sol = preview.solution();
            // 落点（解算用的目标中心）
            BlockPos impact = BlockPos.containing(sol.target());
            graphics.drawString(font, Component.translatable(
                    "screen.dragonrise_reforge.fire_control.impact",
                    impact.getX(), impact.getY(), impact.getZ()
            ), textX, resultY, 0xFF00FF66, false);
            graphics.drawString(font, Component.translatable("screen.dragonrise_reforge.fire_control.range",
                    String.format("%.1f", sol.range())), textX, resultY + 12, 0xFF00FF66, false);
            graphics.drawString(font, Component.translatable("screen.dragonrise_reforge.fire_control.pitch",
                    String.format("%.2f", sol.pitch())), textX, resultY + 24, 0xFF00FF66, false);
            graphics.drawString(font, Component.translatable("screen.dragonrise_reforge.fire_control.yaw",
                    String.format("%.2f", sol.yaw())), textX, resultY + 36, 0xFF00FF66, false);
            graphics.drawString(font, Component.translatable("screen.dragonrise_reforge.fire_control.flight_time",
                    String.format("%.1f", sol.flightTime())), textX, resultY + 48, 0xFF00FF66, false);
        } else if (preview != null) {
            Component statusText = Component.translatable(preview.status().translationKey())
                    .withStyle(ChatFormatting.RED);
            graphics.drawString(font, statusText, textX, resultY, 0xFFFF5555, false);
            if (preview.status() == FireControlStatus.PITCH_LIMIT && preview.hasRequestedPitch()) {
                graphics.drawString(font, Component.translatable(
                        "screen.dragonrise_reforge.fire_control.pitch_detail",
                        String.format("%.1f", preview.requestedPitch()),
                        String.format("%.0f", vehicle.getTurretMinPitch()),
                        String.format("%.0f", vehicle.getTurretMaxPitch())
                ), textX, resultY + 12, 0xFFFFAA55, false);
            }
        }

        int statusY = panelY + PANEL_HEIGHT - 70;
        FireControlStatus status = vehicle.getFireControlStatus();
        ChatFormatting statusColor = status == FireControlStatus.READY ? ChatFormatting.GREEN
                : status == FireControlStatus.INACTIVE ? ChatFormatting.GRAY
                : ChatFormatting.YELLOW;
        graphics.drawString(font, Component.translatable("screen.dragonrise_reforge.fire_control.status")
                .append(": ")
                .append(Component.translatable(status.translationKey()).withStyle(statusColor)),
                textX, statusY, 0xFFFFFFFF, false);

        if (!vehicle.isMainCannonSelected()) {
            graphics.drawString(font, Component.translatable("screen.dragonrise_reforge.fire_control.weapon_warning")
                    .withStyle(ChatFormatting.RED), textX, statusY + 12, 0xFFFF5555, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
