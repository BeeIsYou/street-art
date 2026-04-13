package com.streetart.component;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

public record AreaSelectComponent(BlockPos start) {
    public static StreamCodec<ByteBuf, AreaSelectComponent> BYTE_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            AreaSelectComponent::start,
            AreaSelectComponent::new
    );
}
