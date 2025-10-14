package com.happyghastboost.network;

import com.happyghastboost.HappyGhastBoost;
import com.happyghastboost.data.GhastStaminaData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload for syncing stamina from server to client
 */
public record StaminaSyncPayload(int entityId, float stamina) implements CustomPacketPayload {

    public static final Type<StaminaSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HappyGhastBoost.MOD_ID, "stamina_sync"));

    public static final StreamCodec<ByteBuf, StaminaSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            StaminaSyncPayload::entityId,
            ByteBufCodecs.FLOAT,
            StaminaSyncPayload::stamina,
            StaminaSyncPayload::new
    );

    @Override
    public Type<StaminaSyncPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the client side
     */
    public static void handleClient(StaminaSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(payload.entityId);
                if (entity != null) {
                    GhastStaminaData.setStamina(entity, payload.stamina);
                }
            }
        });
    }
}