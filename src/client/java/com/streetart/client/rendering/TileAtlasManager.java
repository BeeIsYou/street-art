package com.streetart.client.rendering;

import com.mojang.blaze3d.platform.NativeImage;
import com.streetart.StreetArt;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.Random;

public class TileAtlasManager {
    private int entriesX = 128;
    private int entriesY = 128;
    private int idCount = this.entriesX * this.entriesY;

    private float uSize = 1f / this.entriesX;
    private float vSize = 1f / this.entriesY;

    private int nextIndex = 0;
    private int useCount = 0;
    private final IntSet freeIDs = new IntArraySet(this.idCount);

    private final TextureManager textureManager;
    public Identifier atlasLocation;
    private DynamicTexture atlasTexture;

    private boolean dirty;

    public TileAtlasManager(final TextureManager textureManager) {
        this.textureManager = textureManager;
        this.atlasLocation = StreetArt.id("atlas");
        this.atlasTexture = new DynamicTexture(() -> "street_art:atlas_" + this.entriesX, this.entriesX * 16, this.entriesY * 16, true);
        this.textureManager.register(this.atlasLocation, this.atlasTexture);
    }

    public int allocateID() {
        if (this.nextIndex < this.idCount) {
            this.useCount++;
            return this.nextIndex++;
        }
        final OptionalInt id = this.freeIDs.intStream().findFirst();
        if (id.isEmpty()) {
            this.quadrupleSize();
            return this.allocateID();
        }
        this.freeIDs.remove(id.getAsInt());
        this.useCount++;
        return id.getAsInt();
    }

    private void quadrupleSize() {
        final DynamicTexture newTexture = new DynamicTexture(() -> "street_art:atlas_" + this.entriesX, this.entriesX * 32, this.entriesY * 32, true);
        final NativeImage newPix = newTexture.getPixels();
        final NativeImage oldPix = this.atlasTexture.getPixels();
        // puts textures into the atlas in such a way that
        for (int y = 0; y < this.entriesY; y++) {
            if (y % 2 == 0) {
                final int ny = y / 2;
                for (int py = 0; py < 16; py++) {
                    for (int px = 0; px < this.entriesX * 16; px++) {
                        newPix.setPixel(
                                px,
                                ny * 16 + py,
                                oldPix.getPixel(px, y * 16 + py)
                        );
                    }
                }
            } else {
                final int ny = (y - 1) / 2;
                for (int py = 0; py < 16; py++) {
                    for (int px = 0; px < this.entriesX * 16; px++) {
                        newPix.setPixel(
                                px + this.entriesX * 16,
                                ny * 16 + py,
                                oldPix.getPixel(px, y * 16 + py)
                        );
                    }
                }
            }
        }

        this.atlasTexture.close();
        this.atlasTexture = newTexture;
        this.textureManager.register(this.atlasLocation, this.atlasTexture);

        this.entriesX *= 2;
        this.entriesY *= 2;
        this.idCount *= 4;
        this.uSize /= 2;
        this.vSize /= 2;

        StreetArt.LOGGER.info("Resizing spray paint atlas to {}x{}", this.entriesX * 16, this.entriesY * 16);
    }

    public void freeID(final int id) {
        this.freeIDs.add(id);
        this.useCount--;
    }

    public void checkDirty() {
        if (this.dirty) {
            this.dirty = false;
            this.atlasTexture.upload();
        }
    }

    public void clear() {
        this.atlasTexture.setPixels(new NativeImage(this.entriesX * 16, this.entriesY * 16, true));
        this.freeIDs.clear();
        this.nextIndex = 0;
        this.useCount = 0;
    }

    public void writeUVs(final int id, final Vector4f uv) {
        final float x = id % this.entriesX;
        final float y = id / this.entriesY; // intentionally do not cast
        uv.set(
            x / this.entriesX, y / this.entriesY,
            x / this.entriesX + this.uSize, y / this.entriesY + this.vSize
        );
    }

    public static final int COLOR_MASK = 0b00000000_00000011_00000011_00000011;
    private static int SEED = new Random().nextInt(); //kill me I DARE you

    private int nextRandom() {
        SEED ^= SEED << 13;
        SEED ^= SEED >> 17;
        SEED ^= SEED << 5;
        return SEED;
    }

    public void setPixel(final int id, int x, int y, int color) {
        x += (id % this.entriesX) * 16;
        y += (id / this.entriesX) * 16;

        if (color != 0) {
            color = color ^ (COLOR_MASK & this.nextRandom());
        }

        this.atlasTexture.getPixels().setPixel(x, y, color);
        this.dirty = true;
    }

    public int getPixel(final int id, int x, int y) {
        x += (id % this.entriesX) * 16;
        y += (id / this.entriesX) * 16;
        return this.atlasTexture.getPixels().getPixel(x, y);
    }

    public byte[] copyPixelData(final int id) {
        final byte[] data = new byte[16*16*4];
        final ByteBuffer buffer = ByteBuffer.wrap(data);

        final int tx = (id % this.entriesX) * 16;
        final int ty = (id / this.entriesX) * 16;
        for (int y = ty; y < ty + 16; y++) {
            for (int x = tx; x < tx + 16; x++) {
                buffer.putInt(this.atlasTexture.getPixels().getPixel(x, y));
            }
        }
        return data;
    }

    public int getCapacity() {
        return this.idCount;
    }

    public int getSize() {
        return this.useCount;
    }
}
