package com.streetart.client.manager;

import com.streetart.GData;
import com.streetart.client.StreetArtClient;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.TileChange;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector2i;

public class GClientData extends GData implements AutoCloseable {
    public final Direction dir;
    public final BlockPos pos;
    public final int id;

    public int light = -1;

    public GClientData(final Direction dir, final double depth, final BlockPos pos, final int id) {
        super(depth);
        this.dir = dir;
        this.pos = pos;
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

    public void handleChange(final int color, final TileChange tileChange) {
        for (int i = 0; i < 256 / 8; i++) {
            final byte b = tileChange.modifiedPixels()[i];

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
        this.light = LevelRenderer.getLightCoords(level,
                this.getDepth() == 1 ? this.pos.relative(this.dir) : this.pos
        );
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
        final boolean changed = StreetArtClient.textureManager.tileAtlasManager.getPixel(this.id, x, y) != color;
        StreetArtClient.textureManager.tileAtlasManager.setPixel(this.id, x, y, color);
        return changed;
    }

    public int getPixel(final int x, final int y) {
        return StreetArtClient.textureManager.tileAtlasManager.getPixel(this.id, x, y);
    }

    @Override
    public void close() {
        StreetArtClient.textureManager.tileAtlasManager.freeID(this.id);
    }
}
