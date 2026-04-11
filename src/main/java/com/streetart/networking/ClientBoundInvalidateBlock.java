package com.streetart.networking;

import com.streetart.StreetArt;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientBoundInvalidateBlock(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientBoundInvalidateBlock> TYPE  = new CustomPacketPayload.Type<>(StreetArt.id("invalidate_block"));
    public static final StreamCodec<ByteBuf, ClientBoundInvalidateBlock> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientBoundInvalidateBlock::pos,
            ClientBoundInvalidateBlock::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
