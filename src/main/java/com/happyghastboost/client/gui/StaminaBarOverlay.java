package com.happyghastboost.client.gui;

import com.happyghastboost.HappyGhastBoost;
import com.happyghastboost.config.StaminaConfig;
import com.happyghastboost.data.GhastStaminaData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.HappyGhast;

public class StaminaBarOverlay {
    // Sprite locations for stamina icons
    private static final ResourceLocation STAMINA_EMPTY = ResourceLocation.fromNamespaceAndPath(
            HappyGhastBoost.MOD_ID, "stamina_empty");
    private static final ResourceLocation STAMINA_HALF = ResourceLocation.fromNamespaceAndPath(
            HappyGhastBoost.MOD_ID, "stamina_half");
    private static final ResourceLocation STAMINA_FULL = ResourceLocation.fromNamespaceAndPath(
            HappyGhastBoost.MOD_ID, "stamina_full");

    // Icon dimensions
    private static final int ICON_WIDTH = 9;
    private static final int ICON_HEIGHT = 9;

    // Number of icons to display (10 icons = 100 stamina, each icon = 10 stamina)
    private static final int ICON_COUNT = 10;

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();

        // Only render in survival/adventure mode
        if (minecraft.options.hideGui || minecraft.player == null) {
            return;
        }

        // Check if player is riding a Happy Ghast
        Entity vehicle = minecraft.player.getVehicle();

        if (!(vehicle instanceof HappyGhast ghast)) {
            return;
        }

        // Get stamina value
        float stamina = GhastStaminaData.getStamina(ghast);
        int maxStamina = StaminaConfig.MAX_STAMINA.get();

        // Calculate screen position (opposite side from ghast health bar)
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Position on the right side, mirroring the health bar on the left
        int startX = screenWidth / 2 + 10;
        int startY = screenHeight - 49;

        // Render the stamina bar
        renderStaminaBar(guiGraphics, startX, startY, stamina, maxStamina);
    }

    private void renderStaminaBar(GuiGraphics guiGraphics, int x, int y, float stamina, int maxStamina) {
        // Calculate how many full and half icons to show
        float staminaPerIcon = maxStamina / (float) ICON_COUNT;
        int fullIcons = (int) (stamina / staminaPerIcon);
        boolean hasHalfIcon = (stamina % staminaPerIcon) >= (staminaPerIcon / 2);

        // Render from left to right (normal order) so it empties left to right
        for (int i = 0; i < ICON_COUNT; i++) {
            int iconX = x + (i * 8);
            int iconY = y;

            // Calculate from the right side - rightmost icons should be full
            int iconFromRight = ICON_COUNT - 1 - i;

            if (iconFromRight < fullIcons) {
                // Render full icon
                renderIcon(guiGraphics, iconX, iconY, IconType.FULL);
            } else if (iconFromRight == fullIcons && hasHalfIcon) {
                // Render half icon
                renderIcon(guiGraphics, iconX, iconY, IconType.HALF);
            } else {
                // Render empty icon
                renderIcon(guiGraphics, iconX, iconY, IconType.EMPTY);
            }
        }
    }

    private void renderIcon(GuiGraphics guiGraphics, int x, int y, IconType type) {
        ResourceLocation sprite = switch (type) {
            case FULL -> STAMINA_FULL;
            case HALF -> STAMINA_HALF;
            case EMPTY -> STAMINA_EMPTY;
        };

        // Use blitSprite with RenderPipeline
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,  // pipeline
                sprite,                         // sprite location
                x, y,                          // screen position
                ICON_WIDTH,                    // width
                ICON_HEIGHT                    // height
        );
    }

    private enum IconType {
        EMPTY,
        HALF,
        FULL
    }
}