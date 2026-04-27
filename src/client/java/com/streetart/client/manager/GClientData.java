package com.streetart.client.manager;

import com.streetart.client.rendering.GraffitiAtlas;
import com.streetart.client.rendering.LightMath;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.GraffitiChangeData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector2i;

public class GClientData implements AutoCloseable {
    private final GraffitiAtlas atlas;

    public final Direction dir;
    public final BlockPos pos;
    public final int depth;
    public final int id;

    public int color0 = -1;
    public int color1 = -1;
    public int color2 = -1;
    public int color3 = -1;

    public int light0 = -1;
    public int light1 = -1;
    public int light2 = -1;
    public int light3 = -1;

    public GClientData(final GraffitiAtlas atlas, final Direction dir, final int depth, final BlockPos pos, final int id) {
        this.atlas = atlas;
        this.dir = dir;
        this.pos = pos;
        this.depth = depth;;
        this.id = id;
    }

    public void update(final byte[] data) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                final int i = (x + y * 16);

                final ColorComponent component = ColorComponent.BY_ID.apply(data[i]);
                this.setPixel(x, y, component.argb);
            }
        }
    }

    public void handleChange(final int color, final GraffitiChangeData graffitiChangeData) {
        for (int i = 0; i < 256 / 8; i++) {
            final byte b = graffitiChangeData.modifiedPixels()[i];

            for (int j = 0; j < 8; j++) {
                if (((b >>> j) & 1) == 1) {
                    final int pi = i * 8 + j;
                    final int x = pi % 16;
                    final int y = pi / 16;
                    this.setPixel(x, y, color);
                }
            }
        }
    }

    public void updateLight(final ClientLevel level) {
        final LightMath math = new LightMath();
        math.OhGodSoMuchMath(this, level, level.getBlockState(this.pos));
    }

    /**
     * @param coordinates (unchecked) xy coordinate [0,15]x[0,15]
     * @return true if pixel changed
     */
    public boolean applyPixel(final Vector2i coordinates, final int color) {
        final int i = (coordinates.x + coordinates.y * 16);
        if (0 <= i && i < 16 * 16) {
            return this.setPixel(coordinates.x, coordinates.y, color);
        }
        return false;
    }

    /**
     * @param x (unchecked) x coordinate [0,15]
     * @param y (unchecked) y coordinate [0,15]
     * @return true if pixel changed
     */
    public boolean setPixel(final int x, final int y, final int color) {
        final boolean changed = this.atlas.getBasePixel(this.id, x, y) != color;
        this.atlas.setPixel(this.id, x, y, color);
        return changed;
    }

    public int getPixel(final int x, final int y) {
        return this.atlas.getBasePixel(this.id, x, y);
    }

    @Override
    public void close() {
        this.atlas.freeID(this.id);
    }
}
