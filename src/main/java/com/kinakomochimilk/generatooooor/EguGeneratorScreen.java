
package com.kinakomochimilk.generatooooor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class EguGeneratorScreen
        extends AbstractContainerScreen<EguGeneratorMenu> {

    private static final int BACKGROUND = 0xFF171C22;
    private static final int PANEL = 0xFF222A33;
    private static final int PANEL_INNER = 0xFF151B22;
    private static final int BORDER = 0xFF3B4A57;
    private static final int TEXT = 0xFFE8F1F7;
    private static final int MUTED = 0xFF9AABB8;
    private static final int CYAN = 0xFF42D9F5;
    private static final int GREEN = 0xFF61E294;
    private static final int YELLOW = 0xFFFFD166;
    private static final int RED = 0xFFFF657A;

    private static final int ENERGY_BAR_WIDTH = 188;
    private static final int ENERGY_BAR_HEIGHT = 12;

    public EguGeneratorScreen(
            EguGeneratorMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);

        this.imageWidth = 220;
        this.imageHeight = 194;
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;

        long energy = Math.max(0L, menu.getEnergy());
        long capacity = EguGeneratorBlockEntity.MAX_ENERGY;
        long generation = Math.max(0L, menu.getGenerationPerTick());

        double percentage = capacity > 0L
                ? Math.min(1.0, (double) energy / capacity)
                : 0.0;

        // 全体の背景
        graphics.fill(
                left,
                top,
                left + imageWidth,
                top + imageHeight,
                BACKGROUND
        );

        // 外周の枠
        drawBorder(
                graphics,
                left,
                top,
                imageWidth,
                imageHeight,
                BORDER
        );

        // 上部タイトルバー
        graphics.fill(
                left + 1,
                top + 1,
                left + imageWidth - 1,
                top + 25,
                PANEL
        );

        graphics.fill(
                left + 1,
                top + 24,
                left + imageWidth - 1,
                top + 26,
                CYAN
        );

        // エネルギーパネル
        drawPanel(
                graphics,
                left + 10,
                top + 32,
                200,
                66
        );

        // エネルギーゲージの背景
        int barX = left + 16;
        int barY = top + 66;

        graphics.fill(
                barX,
                barY,
                barX + ENERGY_BAR_WIDTH,
                barY + ENERGY_BAR_HEIGHT,
                PANEL_INNER
        );

        drawBorder(
                graphics,
                barX,
                barY,
                ENERGY_BAR_WIDTH,
                ENERGY_BAR_HEIGHT,
                BORDER
        );

        int innerWidth = ENERGY_BAR_WIDTH - 2;
        int filledWidth = (int) Math.round(innerWidth * percentage);
        filledWidth = Math.max(0, Math.min(innerWidth, filledWidth));

        int energyColor;

        if (percentage >= 0.90) {
            energyColor = GREEN;
        } else if (percentage >= 0.25) {
            energyColor = CYAN;
        } else {
            energyColor = YELLOW;
        }

        if (filledWidth > 0) {
            graphics.fill(
                    barX + 1,
                    barY + 1,
                    barX + 1 + filledWidth,
                    barY + ENERGY_BAR_HEIGHT - 1,
                    energyColor
            );

            // ゲージ上部のハイライト
            graphics.fill(
                    barX + 1,
                    barY + 1,
                    barX + 1 + filledWidth,
                    barY + 3,
                    0x99FFFFFF
            );
        }

        // ゲージの目盛り
        for (int i = 1; i < 4; i++) {
            int tickX = barX + ENERGY_BAR_WIDTH * i / 4;

            graphics.fill(
                    tickX,
                    barY,
                    tickX + 1,
                    barY + ENERGY_BAR_HEIGHT,
                    0x553D5362
            );
        }

        // 発電情報パネル
        drawPanel(
                graphics,
                left + 10,
                top + 105,
                200,
                48
        );

        // ステータスパネル
        drawPanel(
                graphics,
                left + 10,
                top + 160,
                200,
                24
        );

        // ステータスインジケーター
        int statusColor;

        if (energy >= capacity) {
            statusColor = YELLOW;
        } else if (generation > 0L) {
            statusColor = GREEN;
        } else {
            statusColor = MUTED;
        }

        graphics.fill(
                left + 17,
                top + 168,
                left + 23,
                top + 176,
                statusColor
        );

        // ゲージ上にマウスがある場合のツールチップ
        if (mouseX >= barX
                && mouseX < barX + ENERGY_BAR_WIDTH
                && mouseY >= barY
                && mouseY < barY + ENERGY_BAR_HEIGHT) {

            graphics.renderTooltip(
                    font,
                    Component.literal(
                            formatEnergy(energy)
                                    + " / "
                                    + formatEnergy(capacity)
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        long energy = Math.max(0L, menu.getEnergy());
        long capacity = EguGeneratorBlockEntity.MAX_ENERGY;
        long generation = Math.max(0L, menu.getGenerationPerTick());

        double percentage = capacity > 0L
                ? Math.min(100.0, (double) energy / capacity * 100.0)
                : 0.0;

        // ヘッダー
        graphics.drawString(
                font,
                "EGU GENERATOR",
                10,
                8,
                CYAN,
                false
        );

        graphics.drawString(
                font,
                "V3",
                imageWidth - 24,
                8,
                MUTED,
                false
        );

        // エネルギー情報
        graphics.drawString(
                font,
                "ENERGY STORAGE",
                16,
                39,
                MUTED,
                false
        );

        String percentageText = String.format(
                Locale.ROOT,
                "%.2f%%",
                percentage
        );

        graphics.drawString(
                font,
                percentageText,
                imageWidth - 16 - font.width(percentageText),
                39,
                TEXT,
                false
        );

        graphics.drawString(
                font,
                formatEnergy(energy),
                16,
                51,
                TEXT,
                false
        );

        String capacityText = "MAX " + formatEnergy(capacity);

        graphics.drawString(
                font,
                capacityText,
                imageWidth - 16 - font.width(capacityText),
                51,
                MUTED,
                false
        );

        // 発電情報
        graphics.drawString(
                font,
                "GENERATION",
                16,
                112,
                MUTED,
                false
        );

        graphics.drawString(
                font,
                formatEnergy(generation) + "/t",
                16,
                127,
                GREEN,
                false
        );

        String generationStatus = generation > 0L
                ? "ACTIVE"
                : "NO FUEL";

        graphics.drawString(
                font,
                generationStatus,
                imageWidth - 16 - font.width(generationStatus),
                127,
                generation > 0L ? GREEN : YELLOW,
                false
        );

        // 機械ステータス
        String status;

        if (energy >= capacity) {
            status = "FULL";
        } else if (generation > 0L) {
            status = "GENERATING";
        } else {
            status = "IDLE";
        }

        int statusColor;

        if (energy >= capacity) {
            statusColor = YELLOW;
        } else if (generation > 0L) {
            statusColor = GREEN;
        } else {
            statusColor = MUTED;
        }

        graphics.drawString(
                font,
                status,
                29,
                167,
                statusColor,
                false
        );

        String machineLabel = "MACHINE STATUS";

        graphics.drawString(
                font,
                machineLabel,
                imageWidth - 16 - font.width(machineLabel),
                167,
                MUTED,
                false
        );
    }

    private void drawPanel(
            GuiGraphics graphics,
            int x,
            int y,
            int panelWidth,
            int panelHeight
    ) {
        graphics.fill(
                x,
                y,
                x + panelWidth,
                y + panelHeight,
                PANEL
        );

        drawBorder(
                graphics,
                x,
                y,
                panelWidth,
                panelHeight,
                BORDER
        );

        // 左端のアクセント
        graphics.fill(
                x + 1,
                y + 1,
                x + 3,
                y + panelHeight - 1,
                CYAN
        );
    }

    private void drawBorder(
            GuiGraphics graphics,
            int x,
            int y,
            int borderWidth,
            int borderHeight,
            int color
    ) {
        graphics.fill(
                x,
                y,
                x + borderWidth,
                y + 1,
                color
        );

        graphics.fill(
                x,
                y + borderHeight - 1,
                x + borderWidth,
                y + borderHeight,
                color
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + borderHeight,
                color
        );

        graphics.fill(
                x + borderWidth - 1,
                y,
                x + borderWidth,
                y + borderHeight,
                color
        );
    }

    private String formatEnergy(long value) {
        if (value >= 1_000_000_000_000L) {
            return String.format(
                    Locale.ROOT,
                    "%.2f TFE",
                    value / 1_000_000_000_000.0
            );
        }

        if (value >= 1_000_000_000L) {
            return String.format(
                    Locale.ROOT,
                    "%.2f GFE",
                    value / 1_000_000_000.0
            );
        }

        if (value >= 1_000_000L) {
            return String.format(
                    Locale.ROOT,
                    "%.2f MFE",
                    value / 1_000_000.0
            );
        }

        if (value >= 1_000L) {
            return String.format(
                    Locale.ROOT,
                    "%.2f kFE",
                    value / 1_000.0
            );
        }

        return value + " FE";
    }
}
