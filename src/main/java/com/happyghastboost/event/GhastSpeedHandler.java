package com.happyghastboost.event;

import com.happyghastboost.common.StaminaMath;
import com.happyghastboost.config.StaminaConfig;
import com.happyghastboost.data.GhastStaminaData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class GhastSpeedHandler {
    private static final float DEFAULT_GHAST_SPEED = 0.05f;

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        // Only process on server side
        if (entity.level().isClientSide()) {
            return;
        }

        // Only process Happy Ghasts
        if (!(entity instanceof HappyGhast ghast)) {
            return;
        }

        // Check if ghast has a player riding it
        boolean hasRider = !ghast.getPassengers().isEmpty() &&
                ghast.getPassengers().get(0) instanceof Player;

        if (hasRider) {
            // Initialize stamina if not present
            if (!GhastStaminaData.hasStamina(ghast)) {
                GhastStaminaData.initializeStamina(ghast);
            }

            // Calculate and apply speed based on stamina
            float stamina = GhastStaminaData.getStamina(ghast);
            float speed = calculateSpeed(stamina);
            applySpeed(ghast, speed);
        } else {
            // Reset to default speed when no rider
            applySpeed(ghast, DEFAULT_GHAST_SPEED);
        }
    }

    private float calculateSpeed(float stamina) {
        return StaminaMath.speedFor(
                stamina,
                StaminaConfig.MAX_STAMINA.get(),
                StaminaConfig.BASE_SPEED.get().floatValue(),
                StaminaConfig.MAX_SPEED.get().floatValue(),
                StaminaConfig.CURVE_STRENGTH.get().floatValue()
        );
    }

    /**
     * Apply speed to ghast's flying movement attribute
     */
    private void applySpeed(HappyGhast ghast, float speed) {
        var flyingSpeedAttr = ghast.getAttribute(Attributes.FLYING_SPEED);
        if (flyingSpeedAttr != null && flyingSpeedAttr.getBaseValue() != speed) {
            flyingSpeedAttr.setBaseValue(speed);
        }
    }
}