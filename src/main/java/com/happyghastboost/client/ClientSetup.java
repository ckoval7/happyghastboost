package com.happyghastboost.client;

import com.happyghastboost.HappyGhastBoost;
import com.happyghastboost.client.gui.StaminaBarOverlay;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = HappyGhastBoost.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        StaminaBarOverlay overlay = new StaminaBarOverlay();
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(HappyGhastBoost.MOD_ID, "stamina_bar"),
                overlay::render
        );
        HappyGhastBoost.LOGGER.info("Stamina bar GUI overlay registered");
    }
}