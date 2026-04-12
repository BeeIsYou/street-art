package com.streetart.client.manager;

import com.google.common.primitives.Ints;
import com.streetart.GData;
import com.streetart.client.StreetArtClient;
import com.streetart.networking.BiDirectionalGraffitiChange;
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

    public GClientData(Direction dir, double depth, BlockPos pos, int id) {
        super(depth);
        this.dir = dir;
        this.pos = pos;
        this.id = id;
    }

    public void update(byte[] data) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int i = (x + y*16) * 4;
                int argb = Ints.fromBytes(data[i], data[i+1], data[i+2], data[i+3]);
                this.setPixel(x, y, argb);
            }
        }
    }

    public void handleChange(int color, BiDirectionalGraffitiChange.TileChange tileChange) {
        for (int i = 0; i < 256/8; i++) {
            byte b = tileChange.modifiedPixels()[i];

            for (int j = 0; j < 8; j++) {
                if (((b >>> j) & 1) == 1) {
                    int pi = i * 8 + j;
                    int x = pi / 16;
                    int y = pi % 16;
                    this.setPixel(x, y, color);
                }
            }
        }
    }

    public void updateLight(ClientLevel level) {
        this.light = LevelRenderer.getLightCoords(level,
                this.getDepth() == 1 ? this.pos.relative(this.dir) : this.pos
        );
    }

    public void applyPixel(Vector2i coordinates, final int color) {
        int i = (coordinates.x + coordinates.y * 16);
        if (0 <= i && i < 16*16) {
            this.setPixel(coordinates.x, coordinates.y, color);
        }
    }

    public void setPixel(int x, int y, int color) {
        StreetArtClient.textureManager.tileAtlasManager.setPixel(this.id, x, y, color);
    }

    public int getPixel(int x, int y) {
        return StreetArtClient.textureManager.tileAtlasManager.getPixel(this.id, x, y);
    }

    @Override
    public void close() {
        StreetArtClient.textureManager.tileAtlasManager.freeID(this.id);
    }
}
