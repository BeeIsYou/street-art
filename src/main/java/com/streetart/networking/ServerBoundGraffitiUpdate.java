package com.streetart.networking;

import com.streetart.StreetArt;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerBoundGraffitiUpdate(BlockPos pos, Direction dir, double depth, byte[] textureData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerBoundGraffitiUpdate> TYPE  = new Type<>(StreetArt.id("server_graffiti_update"));
    public static final StreamCodec<ByteBuf, ServerBoundGraffitiUpdate> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ServerBoundGraffitiUpdate::pos,
            Direction.STREAM_CODEC, ServerBoundGraffitiUpdate::dir,
            ByteBufCodecs.DOUBLE, ServerBoundGraffitiUpdate::depth,
            ByteBufCodecs.BYTE_ARRAY, ServerBoundGraffitiUpdate::textureData,
            ServerBoundGraffitiUpdate::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
