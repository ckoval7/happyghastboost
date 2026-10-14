package com.happyghastboost.event;

import com.happyghastboost.HappyGhastBoost;
import com.happyghastboost.common.FlyingTemptGoal;
import com.happyghastboost.item.ModItems;
import net.minecraft.world.entity.animal.HappyGhast;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class GhastAttractionHandler {

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Only process on server side
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Check if entity is a Happy Ghast
        if (!(event.getEntity() instanceof HappyGhast ghast)) {
            return;
        }

        // Add custom flying tempt goal
        ghast.goalSelector.addGoal(3, new FlyingTemptGoal(ghast, stack -> stack.is(ModItems.GHAST_KIBBLE.get())));

        HappyGhastBoost.LOGGER.debug("Added flying kibble temptation goal to Happy Ghast");
    }
}
