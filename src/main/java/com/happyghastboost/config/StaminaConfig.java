package com.happyghastboost.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class StaminaConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Stamina settings
    public static final ModConfigSpec.DoubleValue DECAY_RATE;
    public static final ModConfigSpec.IntValue MAX_STAMINA;
    public static final ModConfigSpec.IntValue INITIAL_STAMINA;
    public static final ModConfigSpec.IntValue FOOD_RESTORE_AMOUNT;

    // Speed settings
    public static final ModConfigSpec.DoubleValue BASE_SPEED;
    public static final ModConfigSpec.DoubleValue MAX_SPEED;
    public static final ModConfigSpec.DoubleValue CURVE_STRENGTH;

    static {
        BUILDER.push("stamina_settings");

        DECAY_RATE = BUILDER
                .comment("Stamina points lost per second while riding (default: 0.2)")
                .defineInRange("decayRate", 0.2, 0.0, 10.0);

        MAX_STAMINA = BUILDER
                .comment("Maximum stamina points (default: 100)")
                .defineInRange("maxStamina", 100, 1, 1000);

        INITIAL_STAMINA = BUILDER
                .comment("Initial stamina for new Happy Ghasts (default: 0)")
                .defineInRange("initialStamina", 0, 0, 100);

        FOOD_RESTORE_AMOUNT = BUILDER
                .comment("Stamina points restored per Glowstone Dust (default: 20)")
                .defineInRange("foodRestoreAmount", 20, 1, 100);

        BUILDER.pop();

        BUILDER.push("speed_settings");

        BASE_SPEED = BUILDER
                .comment("Base flying speed at 0 stamina (default: 0.05)")
                .defineInRange("baseSpeed", 0.05, 0.01, 1.0);

        MAX_SPEED = BUILDER
                .comment("Maximum flying speed at 100 stamina (default: 0.125)")
                .defineInRange("maxSpeed", 0.125, 0.01, 1.0);

        CURVE_STRENGTH = BUILDER
                .comment("Exponential curve strength for speed calculation (default: 4.0)")
                .comment("Higher values = speed stays high longer, then drops sharply")
                .defineInRange("curveStrength", 2.0, 1.0, 10.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}