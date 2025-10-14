package com.happyghastboost.event;

import com.happyghastboost.HappyGhastBoost;
import com.happyghastboost.config.StaminaConfig;
import com.happyghastboost.data.GhastStaminaData;
import com.happyghastboost.item.ModItems;
import com.happyghastboost.network.StaminaSyncPayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class FeedingHandler {

    @SubscribeEvent
    public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        // Only process on server side
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();

        // Check if interacting with a Happy Ghast
        if (!(event.getTarget() instanceof HappyGhast ghast)) {
            return;
        }

        // Check if player is holding Ghast Kibble
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);

        if (!heldItem.is(ModItems.GHAST_KIBBLE.get())) {
            return;
        }

        // Initialize stamina if not present
        if (!GhastStaminaData.hasStamina(ghast)) {
            GhastStaminaData.initializeStamina(ghast);
        }

        // Get current stamina
        float currentStamina = GhastStaminaData.getStamina(ghast);
        int maxStamina = StaminaConfig.MAX_STAMINA.get();

        // Don't consume item if already at max stamina
        if (currentStamina >= maxStamina) {
            return;
        }

        // Restore stamina (explicitly clamp to max)
        int restoreAmount = StaminaConfig.FOOD_RESTORE_AMOUNT.get();
        float newStamina = Math.min(currentStamina + restoreAmount, maxStamina);
        GhastStaminaData.setStamina(ghast, newStamina);

        // Record feeding time to prevent immediate decay
        GhastStaminaData.setLastFedTime(ghast, event.getLevel().getGameTime());

        HappyGhastBoost.LOGGER.debug("Fed ghast: {} -> {} stamina", currentStamina, newStamina);

        // Sync to all nearby players
        PacketDistributor.sendToPlayersTrackingEntity(
                ghast,
                new StaminaSyncPayload(ghast.getId(), newStamina)
        );

        // Consume item
        if (!player.isCreative()) {
            heldItem.shrink(1);
        }

        // Play sound effect
        event.getLevel().playSound(
                null,
                ghast.getX(),
                ghast.getY(),
                ghast.getZ(),
                SoundEvents.GENERIC_EAT,
                SoundSource.NEUTRAL,
                1.0f,
                1.0f
        );

        // Spawn particle effects
        spawnFeedingParticles((ServerLevel) event.getLevel(), ghast);

        // Cancel further processing
        event.setCanceled(true);
    }

    /**
     * Spawn heart particles around the ghast when fed (instead of glowstone)
     */
    private void spawnFeedingParticles(ServerLevel level, HappyGhast ghast) {
        double x = ghast.getX();
        double y = ghast.getY() + ghast.getBbHeight() / 2.0;
        double z = ghast.getZ();

        // Spawn heart particles in a circle around the ghast
        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;

            level.sendParticles(
                    ParticleTypes.HEART,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0, 0, 0,
                    0.1
            );
        }
    }
}