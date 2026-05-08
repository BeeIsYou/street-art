package com.streetart.managers.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.SplashUtil;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.GraffitiChangeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import org.joml.Vector3d;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

public class GServerDataHolder {

    private static final int PIXEL_BYTE_SIZE = 1;

    public static final Codec<GServerDataHolder> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.BYTE_BUFFER.fieldOf("texture_data").forGetter(d -> d.graffitiData),
                    ExtraCodecs.intRange(0, 15).fieldOf("depth").forGetter(d -> d.depth),
                    Codec.INT.optionalFieldOf("grace", 0).forGetter(d -> d.graceTimer),
                    Direction.CODEC.fieldOf("direction").forGetter(d -> d.dir)
            ).apply(i, GServerDataHolder::new));

    private final ByteBuffer graffitiData;

    /**
     * Number of random tick attempts where no decay will happen
     */
    private int graceTimer;

    /**
     * value from 0-15 representing its "depth"<br>
     * 0 is on the face, 15 is 15 pixels "into" it<br>
     * e.g. a trapdoor would have a depth of 13
     */
    public final int depth;

    public final Direction dir;

    public GServerDataHolder(final int depth, final Direction dir) {
        this(ByteBuffer.allocate(PIXEL_BYTE_SIZE * 16 * 16), depth, 0, dir);
    }

    public GServerDataHolder(final ByteBuffer buf, final int depth, final int graceTimer, final Direction dir) {
        this.depth = depth;
        this.graffitiData = buf;
        this.graceTimer = graceTimer;
        this.dir = dir;
    }

    /**
     * Byte array representing a texture. Synchronized to clients when changed on the following tick.
     */
    public ByteBuffer getGraffitiData() {
        return this.graffitiData;
    }

    /**
     * @return true if fully cleared
     */
    public boolean handleChange(final byte content, final GraffitiChangeData graffitiChangeData) {
        final ByteBuffer buf = this.getGraffitiData();
        buf.position(0);
        for (int i = 0; i < 256 / 8; i++) {
            final byte b = graffitiChangeData.modifiedPixels()[i];

            for (int j = 0; j < 8; j++) {
                if (((b >>> j) & 1) == 1) {
                    setByte(content, buf);
                } else {
                    buf.position(buf.position() + PIXEL_BYTE_SIZE);
                }
            }
        }
        if (content == ColorComponent.CLEAR.id) {
            return this.checkEmpty();
        }
        return false;
    }

    public void set(final byte content, final int x, final int y) {
        final ByteBuffer buf = this.getGraffitiData();
        buf.position((x + y * 16) * PIXEL_BYTE_SIZE);
        setByte(content, buf);
    }

    private static void setByte(final byte content, final ByteBuffer buf) {
        buf.put(content);
    }

    public void fillFromTo(final byte content, final int x1, final int y1, final int x2, final int y2) {
        final ByteBuffer buf = this.getGraffitiData();
        for (int y = y1; y < y2; y++) {
            buf.position(y * 16 + x1);
            for (int x = x1; x < x2; x++) {
                setByte(content, buf);
            }
        }
    }

    public void partialFillFromTo(final byte content, final int x1, final int y1, final int x2, final int y2,
                                  final Vector4f gradient, final BlockPos thisPos, final SplashUtil.VariableThreshold threshold) {
        final Vector3d basePos = new Vector3d(thisPos.getX(), thisPos.getY(), thisPos.getZ());
        if (this.dir.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            basePos.fma((16 - this.depth) / 16d, this.dir.getUnitVec3f());
        } else {
            basePos.fma(this.depth / 16d, this.dir.getUnitVec3f());
        }
        // the consequences of just using the first posestack transformation that works
        final Vector3d dx = switch (this.dir) {
            case DOWN ->  new Vector3d(1 , 0 , 0 );
            case UP ->    new Vector3d(1 , 0 , 0 );
            case NORTH -> new Vector3d(-1, 0 , 0 );
            case SOUTH -> new Vector3d(1 , 0 , 0 );
            case WEST ->  new Vector3d(0 , 0 , 1 );
            case EAST ->  new Vector3d(0 , 0 , -1);
        };
        final Vector3d dy = switch (this.dir) {
            case DOWN ->  new Vector3d(0 , 0 , -1);
            case UP ->    new Vector3d(0 , 0 , 1 );
            case NORTH -> new Vector3d(0 , -1, 0 );
            case SOUTH -> new Vector3d(0 , -1, 0 );
            case WEST ->  new Vector3d(0 , -1, 0 );
            case EAST ->  new Vector3d(0 , -1, 0 );
        };
        if (dx.x + dy.x < 0) {
            basePos.x += 1;
        }
        if (dx.y + dy.y < 0) {
            basePos.y += 1;
        }
        if (dx.z + dy.z < 0) {
            basePos.z += 1;
        }
        final Vector3d duckPos = new Vector3d();

        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                final float py = y / 16f;
                final float px = x / 16f;
                duckPos.set(basePos).fma(px, dx).fma(py, dy);
                final float tx = gradient.x * px + gradient.y * (1 - px);
                final float lx = gradient.z * px + gradient.w * (1 - px);
                final float exposure = tx * py + lx * (1 - py);
                if (threshold.get(duckPos.x, duckPos.y, duckPos.z, this.dir) < exposure) {
                    this.set(content, x, y);
                }
            }
        }
    }

    public void refreshGrace() {
        this.graceTimer = 8;
    }

    /**
     * @return true if all values are updated to zero (transparent)
     */
    public boolean randomDecay(final RandomSource random) {
        if (this.graceTimer > 0) {
            this.graceTimer--;
            return false;
        }

        for (int i = 0; i < 6; i++) {
            final int x = random.nextInt(16);
            final int y = random.nextInt(16);
            this.set((byte) 0, x, y);
        }

        return this.checkEmpty();
    }

    /**
     * @return true if all values are zero (transparent)
     */
    public boolean checkEmpty() {
        final ByteBuffer buf = this.getGraffitiData();
        buf.position(0);
        for (int i = 0; i < 16 * 16; i++) {
            if (buf.get() != 0) {
                return false;
            }
        }

        return true;
    }

    public GServerDataHolder copy() {
        final ByteBuffer clone = ByteBuffer.allocate(this.getGraffitiData().capacity());
        clone.put(this.getGraffitiData().asReadOnlyBuffer().flip());
        return new GServerDataHolder(clone, this.depth, this.graceTimer, this.dir);
    }
}
