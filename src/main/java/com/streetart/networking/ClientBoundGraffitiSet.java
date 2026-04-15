package com.streetart.networking;

import com.streetart.StreetArt;
import com.streetart.managers.data.TempData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundGraffitiSet(BlockPos pos, Direction dir, double depth, byte[] textureData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientBoundGraffitiSet> TYPE  = new Type<>(StreetArt.id("client_graffiti_update"));
    public static final StreamCodec<ByteBuf, ClientBoundGraffitiSet> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientBoundGraffitiSet::pos,
            Direction.STREAM_CODEC, ClientBoundGraffitiSet::dir,
            ByteBufCodecs.DOUBLE, ClientBoundGraffitiSet::depth,
            ByteBufCodecs.BYTE_ARRAY, ClientBoundGraffitiSet::textureData,
            ClientBoundGraffitiSet::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static CustomPacketPayload getSetPacket(final TempData tempData) {
        return new ClientBoundGraffitiSet(
                tempData.pos(),
                tempData.dir(),
                tempData.data().getDepth(),
                tempData.data().getGraffitiData().array()
        );
    }

    public static CustomPacketPayload getSmotheredPacket(final TempData tempData) {
        return new ClientBoundGraffitiSet(
                tempData.pos(),
                tempData.dir(),
                tempData.data().getDepth(),
                new byte[0]
        );
    }
}
