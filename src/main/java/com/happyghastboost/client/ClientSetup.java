package com.happyghastboost.client;

import com.happyghastboost.HappyGhastBoost;
import com.happyghastboost.client.gui.StaminaBarOverlay;
import com.happyghastboost.data.GhastStaminaData;
import com.happyghastboost.network.StaminaSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        // Runs on the client main thread by default
        event.register(StaminaSyncPayload.TYPE, ClientSetup::handleStaminaSync);
    }

    private static void handleStaminaSync(StaminaSyncPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Entity entity = minecraft.level.getEntity(payload.entityId());
            if (entity != null) {
                GhastStaminaData.setStamina(entity, payload.stamina());
            }
        }
    }
}
