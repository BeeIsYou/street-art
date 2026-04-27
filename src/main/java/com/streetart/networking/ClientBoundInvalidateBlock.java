package com.streetart.networking;

import com.streetart.StreetArt;
import com.streetart.managers.data.ExposedGraffitiData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record ClientBoundInvalidateBlock(BlockPos pos, Optional<Identifier> layer) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientBoundInvalidateBlock> TYPE  = new CustomPacketPayload.Type<>(StreetArt.id("invalidate_block"));
    public static final StreamCodec<ByteBuf, ClientBoundInvalidateBlock> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientBoundInvalidateBlock::pos,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), ClientBoundInvalidateBlock::layer,
            ClientBoundInvalidateBlock::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static CustomPacketPayload getPacket(final ExposedGraffitiData exposedGraffitiData) {
        return new ClientBoundInvalidateBlock(exposedGraffitiData.pos(), Optional.empty());
    }

    public static CustomPacketPayload getPacket(final ExposedGraffitiData exposedGraffitiData, final Identifier layer) {
        return new ClientBoundInvalidateBlock(exposedGraffitiData.pos(), Optional.ofNullable(layer));
    }
}
