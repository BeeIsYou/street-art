package com.streetart.graffiti_data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * An individual plane being modified
 *
 * @param modifiedPixels a 1024 bit (32 byte) mask for where to apply
 */
public record TileChange(byte[] modifiedPixels) {
    public static final StreamCodec<ByteBuf, TileChange> CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY, TileChange::modifiedPixels,
            TileChange::new
    );

    public static TileChange empty() {
        return new TileChange(new byte[32]);
    }

    public void markChanged(int x, int y) {
        int i = x + y * 16;
        this.modifiedPixels[i >> 3] |= (byte) (1 << (i & 7));
    }
}
