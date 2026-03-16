package com.rhythm.dragon_vehicle_deployer.client.screen;

import com.rhythm.dragon_vehicle_deployer.menu.DeployerConfigMenu;
import com.rhythm.dragon_vehicle_deployer.network.DeployerSettingsPacket;
import com.rhythm.dragon_vehicle_deployer.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeployerConfigScreen extends AbstractContainerScreen<DeployerConfigMenu> {
    private int localInterval = -1;
    private int localAutoSpawn = -1; // -1=not modified, 0=off, 1=on

    public DeployerConfigScreen(DeployerConfigMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 250;
        this.imageHeight = 150;
    }

    private int getDisplayInterval() {
        return localInterval >= 0 ? localInterval : this.menu.getSpawnIntervalSeconds();
    }

    private boolean getDisplayAutoSpawn() {
        return localAutoSpawn >= 0 ? localAutoSpawn == 1 : this.menu.isAutoSpawnEnabled();
    }

    @Override
    protected void init() {
        super.init();

        int cx = this.leftPos + this.imageWidth / 2;
        int btnW = 32;
        int btnH = 20;
        int gap = 6;

        // --- Row 1: Spawn interval ---
        int row1Y = this.topPos + 35;
        addRenderableWidget(Button.builder(Component.literal("-60"), b -> { localInterval = Math.max(5, getDisplayInterval() - 60); })
                .bounds(cx - 80 - btnW, row1Y, btnW, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("-5"), b -> { localInterval = Math.max(5, getDisplayInterval() - 5); })
                .bounds(cx - 80 + btnW + gap, row1Y, btnW, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("+5"), b -> { localInterval = Math.min(3600, getDisplayInterval() + 5); })
                .bounds(cx + 80 - btnW - btnW - gap, row1Y, btnW, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("+60"), b -> { localInterval = Math.min(3600, getDisplayInterval() + 60); })
                .bounds(cx + 80, row1Y, btnW, btnH).build());

        // --- Row 2: Auto spawn toggle ---
        int row2Y = row1Y + 45;
        addRenderableWidget(Button.builder(Component.translatable("gui.dragonrise_reforge.toggle"), b -> {
            localAutoSpawn = getDisplayAutoSpawn() ? 0 : 1;
        }).bounds(cx - 40, row2Y, 80, btnH).build());

        // --- Row 3: Confirm button ---
        int row3Y = row2Y + 30;
        addRenderableWidget(Button.builder(Component.translatable("gui.dragonrise_reforge.confirm"), b -> {
            ModNetwork.CHANNEL.sendToServer(new DeployerSettingsPacket(
                    this.menu.getPos(), getDisplayInterval(), getDisplayAutoSpawn()));
            this.onClose();
        }).bounds(cx - 50, row3Y, 100, btnH).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xCC222222);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + 1, 0xFF888888);
        graphics.fill(this.leftPos, this.topPos + this.imageHeight - 1, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF888888);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + 1, this.topPos + this.imageHeight, 0xFF888888);
        graphics.fill(this.leftPos + this.imageWidth - 1, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF888888);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int cx = this.leftPos + this.imageWidth / 2;

        // Title
        graphics.drawCenteredString(this.font,
                Component.translatable("container.dragonrise_reforge.deployer_config"),
                cx, this.topPos + 8, 0xFFFFFF);

        // Row 1: Spawn interval
        int row1LabelY = this.topPos + 25;
        int row1Y = this.topPos + 35;
        graphics.drawString(this.font,
                Component.translatable("gui.dragonrise_reforge.spawn_interval"),
                this.leftPos + 8, row1LabelY, 0xAAAAAA, false);
        graphics.drawCenteredString(this.font, getDisplayInterval() + "s", cx, row1Y + 6, 0x55FF55);

        // Row 2: Auto spawn
        int row2LabelY = row1Y + 35;
        graphics.drawString(this.font,
                Component.translatable("gui.dragonrise_reforge.auto_spawn"),
                this.leftPos + 8, row2LabelY, 0xAAAAAA, false);
        boolean autoOn = getDisplayAutoSpawn();
        String statusText = autoOn
                ? Component.translatable("gui.dragonrise_reforge.enabled").getString()
                : Component.translatable("gui.dragonrise_reforge.disabled").getString();
        int statusColor = autoOn ? 0x55FF55 : 0xFF5555;
        int statusX = this.leftPos + 8 + this.font.width(Component.translatable("gui.dragonrise_reforge.auto_spawn")) + 6;
        graphics.drawString(this.font, statusText, statusX, row2LabelY, statusColor, false);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
