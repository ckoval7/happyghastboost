package com.happyghastboost.data;

import com.happyghastboost.HappyGhastBoost;
import com.happyghastboost.common.StaminaMath;
import com.happyghastboost.config.StaminaConfig;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class GhastStaminaData {
    // Registry for attachment types
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HappyGhastBoost.MOD_ID);

    // Codec for stamina data serialization
    private static final Codec<Float> STAMINA_CODEC = Codec.FLOAT;
    private static final com.mojang.serialization.MapCodec<Float> STAMINA_MAP_CODEC =
            STAMINA_CODEC.fieldOf("stamina");

    // Codec for last fed time
    private static final Codec<Long> LAST_FED_CODEC = Codec.LONG;
    private static final com.mojang.serialization.MapCodec<Long> LAST_FED_MAP_CODEC =
            LAST_FED_CODEC.fieldOf("last_fed");

    // Stamina attachment type with codec-based serialization
    public static final Supplier<AttachmentType<Float>> STAMINA = ATTACHMENT_TYPES.register(
            "stamina",
            () -> AttachmentType.builder(() -> StaminaConfig.INITIAL_STAMINA.get().floatValue())
                    .serialize(STAMINA_MAP_CODEC)
                    .build()
    );

    // Last fed time attachment (in game ticks)
    public static final Supplier<AttachmentType<Long>> LAST_FED_TIME = ATTACHMENT_TYPES.register(
            "last_fed_time",
            () -> AttachmentType.builder(() -> 0L)
                    .serialize(LAST_FED_MAP_CODEC)
                    .build()
    );

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
        HappyGhastBoost.LOGGER.info("Registered stamina data attachments");
    }

    /**
     * Get stamina from entity, initializing if not present
     */
    public static float getStamina(Entity entity) {
        return entity.getData(STAMINA);
    }

    /**
     * Set stamina on entity, clamped to valid range
     */
    public static void setStamina(Entity entity, float stamina) {
        float clamped = StaminaMath.clamp(stamina, StaminaConfig.MAX_STAMINA.get().floatValue());
        entity.setData(STAMINA, clamped);
    }

    /**
     * Modify stamina by delta amount
     */
    public static void modifyStamina(Entity entity, float delta) {
        float current = getStamina(entity);
        setStamina(entity, current + delta);
    }

    /**
     * Check if entity has stamina data initialized
     */
    public static boolean hasStamina(Entity entity) {
        return entity.hasData(STAMINA);
    }

    /**
     * Initialize stamina for a new entity
     */
    public static void initializeStamina(Entity entity) {
        if (!hasStamina(entity)) {
            setStamina(entity, StaminaConfig.INITIAL_STAMINA.get().floatValue());
        }
    }

    /**
     * Set the last fed time (in game ticks)
     */
    public static void setLastFedTime(Entity entity, long time) {
        entity.setData(LAST_FED_TIME, time);
    }

    /**
     * Get the last fed time (in game ticks)
     */
    public static long getLastFedTime(Entity entity) {
        return entity.getData(LAST_FED_TIME);
    }
}