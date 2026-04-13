package com.streetart.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.GData;
import com.streetart.graffiti_data.TileChange;
import net.minecraft.util.RandomSource;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

public class GServerDataHolder extends GData {

    public static final Codec<GServerDataHolder> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.BYTE_BUFFER.fieldOf("texture_data").forGetter(d -> d.graffitiData),
                    Codec.DOUBLE.fieldOf("depth").forGetter(GData::getDepth),
                    Codec.INT.optionalFieldOf("grace", 0).forGetter(d -> d.graceTimer)
            ).apply(i, GServerDataHolder::new)
    );

    private final ByteBuffer graffitiData;
    /**
     * Number of random tick attempts where no decay will happen
     */
    private int graceTimer;

    public GServerDataHolder(final double depth) {
        this(ByteBuffer.allocate(4 * 16 * 16), depth, 0);
    }

    public GServerDataHolder(final ByteBuffer buf, final double depth, final int graceTimer) {
        super(depth);
        this.graffitiData = buf;
        this.graceTimer = graceTimer;
    }

    /**
     * Byte array representing a texture. Synchronized to clients when changed on the following tick.
     */
    public ByteBuffer getGraffitiData() {
        return this.graffitiData;
    }

    public void handleChange(final int color, final TileChange tileChange) {
        final ByteBuffer buf = this.getGraffitiData();
        buf.position(0);
        for (int i = 0; i < 256/8; i++) {
            final byte b = tileChange.modifiedPixels()[i];

            for (int j = 0; j < 8; j++) {
                if (((b >>> j) & 1) == 1) {
                    buf.putInt(color);
                } else {
                    buf.position(buf.position() + 4);
                }
            }
        }
    }

    public void set(final int color, final int x, final int y) {
        final ByteBuffer buf = this.getGraffitiData();
        buf.position((x + y*16)*4);
        buf.putInt(color);
    }

    public void fillFromTo(final int color, final int x1, final int y1, final int x2, final int y2) {
        final ByteBuffer buf = this.getGraffitiData();
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                buf.position((x + y*16)*4);
                buf.putInt(color);
            }
        }
    }

    public void partialFillFromTo(final int color, final int x1, final int y1, final int x2, final int y2,
                                  final Vector4f gradient, final RandomSource random) {
        final ByteBuffer buf = this.getGraffitiData();
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                final float py = y / 16f;
                final float px = x / 16f;
                final float tx = gradient.x * px + gradient.y * (1 - px);
                final float lx = gradient.z * px + gradient.w * (1 - px);
                final float exposure = tx * py + lx * (1 - py);
                if (random.nextFloat() < exposure) {
                    buf.position((x + y*16)*4);
                    buf.putInt(color);
                }
            }
        }
    }

    public void refreshGrace() {
        this.graceTimer = 50;
    }

    /**
     * @return true if all values are updated to zero (transparent)
     */
    public boolean randomDecay(RandomSource random) {
        if (this.graceTimer > 0) {
            this.graceTimer--;
            return false;
        }

        for (int i = 0; i < 3; i++) {
            int x = random.nextInt(16);
            int y = random.nextInt(16);
            this.set(0, x, y);
        }
        return this.checkEmpty();
    }

    /**
     * @return true if all values are zero (transparent)
     */
    public boolean checkEmpty() {
        ByteBuffer buf = this.getGraffitiData();
        buf.position(0);
        for (int i = 0; i < 16 * 16; i++) {
            if (buf.getInt() != 0) {
                return false;
            }
        }
        return true;
    }
}
