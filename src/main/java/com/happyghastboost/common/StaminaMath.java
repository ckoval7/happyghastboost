package com.happyghastboost.common;

/**
 * Loader-independent stamina and speed calculations.
 * Keep this free of NeoForge/Fabric imports so it can be shared across loaders.
 */
public final class StaminaMath {
    private StaminaMath() {}

    public static float clamp(float stamina, float maxStamina) {
        return Math.max(0, Math.min(stamina, maxStamina));
    }

    /**
     * Exponential curve: speed stays high, then drops rapidly at low stamina.
     */
    public static float speedFor(float stamina, float maxStamina, float baseSpeed, float maxSpeed, float curveStrength) {
        float normalizedStamina = stamina / maxStamina;
        float curve = 1.0f - (float) Math.exp(-curveStrength * normalizedStamina);
        return baseSpeed + curve * (maxSpeed - baseSpeed);
    }
}
