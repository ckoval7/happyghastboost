package com.happyghastboost.item;

import com.happyghastboost.HappyGhastBoost;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HappyGhastBoost.MOD_ID);

    // Ghast Kibble - food for Happy Ghasts
    public static final DeferredItem<Item> GHAST_KIBBLE =
            ITEMS.registerSimpleItem("ghast_kibble");

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        HappyGhastBoost.LOGGER.info("Registered mod items");
    }
}