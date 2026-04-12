package com.streetart.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.GData;

import java.nio.ByteBuffer;

public class GServerDataHolder extends GData {

    public static final Codec<GServerDataHolder> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.BYTE_BUFFER.fieldOf("texture_data").forGetter(d -> d.graffitiData),
                    Codec.DOUBLE.fieldOf("depth").forGetter(GData::getDepth))
            .apply(i, GServerDataHolder::new)
    );

    private final ByteBuffer graffitiData;

    public GServerDataHolder(final double depth) {
        this(ByteBuffer.allocate(4 * 16 * 16), depth);
    }

    public GServerDataHolder(final ByteBuffer buf, final double depth) {
        super(depth);
        this.graffitiData = buf;
    }

    /**
     * Byte array representing a texture. Synchronized to clients when changed on the following tick.
     */
    public ByteBuffer getGraffitiData() {
        return this.graffitiData;
    }
}
