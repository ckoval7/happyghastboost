package com.happyghastboost.event;

import com.happyghastboost.HappyGhastBoost;
import com.happyghastboost.item.ModItems;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.EnumSet;

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
        ghast.goalSelector.addGoal(3, new FlyingTemptGoal(ghast));

        HappyGhastBoost.LOGGER.debug("Added flying kibble temptation goal to Happy Ghast");
    }

    /**
     * Custom goal for flying entities to follow players holding food
     */
    private static class FlyingTemptGoal extends Goal {
        private final HappyGhast ghast;
        private Player targetPlayer;
        private int delayCounter;
        private static final double FOLLOW_RANGE = 16.0;
        private static final double MIN_DISTANCE = 4.0;
        private static final double SPEED = 0.08;

        public FlyingTemptGoal(HappyGhast ghast) {
            this.ghast = ghast;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // Check periodically to reduce performance impact
            if (this.delayCounter++ % 20 != 0) {
                return false;
            }

            // Find nearby player holding kibble
            this.targetPlayer = this.ghast.level()
                    .getNearestPlayer(this.ghast, FOLLOW_RANGE);

            if (this.targetPlayer == null) {
                return false;
            }

            // Check if player is holding kibble
            return this.isHoldingKibble(this.targetPlayer);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
                return false;
            }

            // Check if still in range
            double distance = this.ghast.distanceToSqr(this.targetPlayer);
            if (distance > FOLLOW_RANGE * FOLLOW_RANGE) {
                return false;
            }

            // Check if still holding kibble
            return this.isHoldingKibble(this.targetPlayer);
        }

        @Override
        public void start() {
            this.delayCounter = 0;
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            // Look at the player
            this.ghast.getLookControl().setLookAt(
                    this.targetPlayer,
                    this.ghast.getMaxHeadYRot() + 20,
                    this.ghast.getMaxHeadXRot()
            );

            double distance = this.ghast.distanceToSqr(this.targetPlayer);

            // Only move if not too close
            if (distance > MIN_DISTANCE * MIN_DISTANCE) {
                // Calculate direction to player
                Vec3 ghastPos = this.ghast.position();
                Vec3 playerPos = this.targetPlayer.position();
                Vec3 direction = playerPos.subtract(ghastPos).normalize();

                // Apply movement with speed
                Vec3 movement = direction.scale(SPEED);
                this.ghast.setDeltaMovement(
                        this.ghast.getDeltaMovement().add(movement)
                );
            }
        }

        private boolean isHoldingKibble(Player player) {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            return mainHand.is(ModItems.GHAST_KIBBLE.get()) ||
                    offHand.is(ModItems.GHAST_KIBBLE.get());
        }
    }
}