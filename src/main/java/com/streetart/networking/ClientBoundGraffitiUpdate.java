package com.streetart.networking;

import com.streetart.StreetArt;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundGraffitiUpdate(BlockPos pos, Direction dir, double depth, byte[] textureData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientBoundGraffitiUpdate> TYPE  = new Type<>(StreetArt.id("client_graffiti_update"));
    public static final StreamCodec<ByteBuf, ClientBoundGraffitiUpdate> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientBoundGraffitiUpdate::pos,
            Direction.STREAM_CODEC, ClientBoundGraffitiUpdate::dir,
            ByteBufCodecs.DOUBLE, ClientBoundGraffitiUpdate::depth,
            ByteBufCodecs.BYTE_ARRAY, ClientBoundGraffitiUpdate::textureData,
            ClientBoundGraffitiUpdate::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
