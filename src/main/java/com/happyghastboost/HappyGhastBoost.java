package com.happyghastboost;

import com.happyghastboost.config.StaminaConfig;
import com.happyghastboost.data.GhastStaminaData;
import com.happyghastboost.event.FeedingHandler;
import com.happyghastboost.event.GhastAttractionHandler;
import com.happyghastboost.event.GhastSpeedHandler;
import com.happyghastboost.event.StaminaDecayHandler;
import com.happyghastboost.item.ModItems;
import com.happyghastboost.network.StaminaSyncPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(HappyGhastBoost.MOD_ID)
public class HappyGhastBoost {
    public static final String MOD_ID = "happyghastboost";
    public static final Logger LOGGER = LoggerFactory.getLogger(HappyGhastBoost.class);

    public HappyGhastBoost(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Happy Ghast Boost mod");

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, StaminaConfig.SPEC);

        // Register items
        ModItems.register(modEventBus);

        // Register data attachments
        GhastStaminaData.register(modEventBus);

        // Register network packets
        modEventBus.addListener(this::registerPayloads);

        // Register event handlers
        NeoForge.EVENT_BUS.register(new StaminaDecayHandler());
        NeoForge.EVENT_BUS.register(new GhastSpeedHandler());
        NeoForge.EVENT_BUS.register(new FeedingHandler());
        NeoForge.EVENT_BUS.register(new GhastAttractionHandler());

        LOGGER.info("Happy Ghast Boost mod initialized successfully");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                StaminaSyncPayload.TYPE,
                StaminaSyncPayload.STREAM_CODEC,
                StaminaSyncPayload::handleClient
        );
        LOGGER.info("Registered stamina sync packet");
    }
}