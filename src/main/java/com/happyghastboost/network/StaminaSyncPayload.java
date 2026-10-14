package com.happyghastboost.network;

import com.happyghastboost.HappyGhastBoost;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

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
}