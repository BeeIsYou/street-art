package com.streetart.networking;

import com.streetart.StreetArt;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundGraffitUpdate(BlockPos pos, Direction dir, double depth, byte[] textureData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientBoundGraffitUpdate> TYPE  = new Type<>(StreetArt.id("graffiti_update"));
    public static final StreamCodec<ByteBuf, ClientBoundGraffitUpdate> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientBoundGraffitUpdate::pos,
            Direction.STREAM_CODEC, ClientBoundGraffitUpdate::dir,
            ByteBufCodecs.DOUBLE, ClientBoundGraffitUpdate::depth,
            ByteBufCodecs.BYTE_ARRAY, ClientBoundGraffitUpdate::textureData,
            ClientBoundGraffitUpdate::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
