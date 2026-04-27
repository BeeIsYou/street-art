package com.streetart.networking;

import com.streetart.StreetArt;
import com.streetart.managers.data.ExposedGraffitiData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record ClientBoundGraffitiSet(Optional<Identifier> layer, BlockPos pos, Direction dir, int depth, byte[] textureData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientBoundGraffitiSet> TYPE  = new Type<>(StreetArt.id("client_graffiti_update"));
    public static final StreamCodec<ByteBuf, ClientBoundGraffitiSet> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), ClientBoundGraffitiSet::layer,
            BlockPos.STREAM_CODEC, ClientBoundGraffitiSet::pos,
            Direction.STREAM_CODEC, ClientBoundGraffitiSet::dir,
            ByteBufCodecs.INT, ClientBoundGraffitiSet::depth,
            ByteBufCodecs.BYTE_ARRAY, ClientBoundGraffitiSet::textureData,
            ClientBoundGraffitiSet::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static CustomPacketPayload getSetPacket(final ExposedGraffitiData exposedGraffitiData) {
        assert exposedGraffitiData.data() != null;
        return new ClientBoundGraffitiSet(
                Optional.ofNullable(exposedGraffitiData.layer()),
                exposedGraffitiData.pos(),
                exposedGraffitiData.dir(),
                exposedGraffitiData.data().depth,
                exposedGraffitiData.data().getGraffitiData().array()
        );
    }

    public static CustomPacketPayload getSmotheredPacket(final ExposedGraffitiData exposedGraffitiData) {
        assert exposedGraffitiData.data() != null;
        return new ClientBoundGraffitiSet(
                Optional.empty(),
                exposedGraffitiData.pos(),
                exposedGraffitiData.dir(),
                exposedGraffitiData.data().depth,
                new byte[0]
        );
    }
}
