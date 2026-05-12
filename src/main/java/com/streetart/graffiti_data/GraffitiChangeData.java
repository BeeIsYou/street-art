package com.streetart.graffiti_data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * An individual plane being modified
 *
 * @param modifiedPixels a 256 bit (32 byte) mask for where to apply
 */
public record GraffitiChangeData(byte[] modifiedPixels) {
    public static final StreamCodec<ByteBuf, GraffitiChangeData> CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY, GraffitiChangeData::modifiedPixels,
            GraffitiChangeData::new
    );

    public static GraffitiChangeData empty() {
        return new GraffitiChangeData(new byte[32]);
    }

    public void markChanged(int x, int y) {
        int i = x + y * 16;
        this.modifiedPixels[i >> 3] |= (byte) (1 << (i & 7));
    }
}
