package com.happyghastboost.event;

import com.happyghastboost.HappyGhastBoost;
import com.happyghastboost.config.StaminaConfig;
import com.happyghastboost.data.GhastStaminaData;
import com.happyghastboost.network.StaminaSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.HappyGhast;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class StaminaDecayHandler {
    private static final int TICKS_PER_SECOND = 20;
    // Grace period after feeding before decay starts (3 seconds)
    private static final int FEEDING_GRACE_PERIOD_TICKS = 60;
    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;

        // Process decay once per second
        if (tickCounter >= TICKS_PER_SECOND) {
            tickCounter = 0;
            processStaminaDecay(event.getServer());
        }
    }

    private void processStaminaDecay(net.minecraft.server.MinecraftServer server) {
        server.getAllLevels().forEach(level -> {
            level.players().forEach(player -> {
                Entity vehicle = player.getVehicle();

                // Check if player is riding a Happy Ghast
                if (vehicle instanceof HappyGhast ghast) {
                    // Initialize stamina if not present
                    if (!GhastStaminaData.hasStamina(ghast)) {
                        GhastStaminaData.initializeStamina(ghast);
                    }

                    // Check if within grace period after feeding
                    long currentTime = level.getGameTime();
                    long lastFedTime = GhastStaminaData.getLastFedTime(ghast);
                    if (currentTime - lastFedTime < FEEDING_GRACE_PERIOD_TICKS) {
                        // Skip decay during grace period
                        return;
                    }

                    // Decay stamina
                    float decayAmount = StaminaConfig.DECAY_RATE.get().floatValue();
                    float currentStamina = GhastStaminaData.getStamina(ghast);

                    if (currentStamina > 0) {
                        GhastStaminaData.modifyStamina(ghast, -decayAmount);
                        float newStamina = GhastStaminaData.getStamina(ghast);

                        // Sync to client
                        if (player instanceof ServerPlayer serverPlayer) {
                            PacketDistributor.sendToPlayer(
                                    serverPlayer,
                                    new StaminaSyncPayload(ghast.getId(), newStamina)
                            );
                        }

                        // Log for debugging every 20 stamina points
                        if ((int)currentStamina % 20 == 0 && (int)currentStamina != (int)newStamina) {
                            HappyGhastBoost.LOGGER.debug("Ghast stamina decayed to: {}", newStamina);
                        }
                    }
                }
            });
        });
    }
}