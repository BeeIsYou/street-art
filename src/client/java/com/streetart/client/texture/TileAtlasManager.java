package com.streetart.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.streetart.StreetArt;
import com.streetart.client.StreetArtClient;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

public class TileAtlasManager {
    // 128x128 = 16384 tiles
    // A 1024x1024 texture, ~4.2MB
    // if updating once per tick, uses 83MB/s memory bandwith
    public static final int ENTRIES_X = 128;
    public static final int ENTRIES_Y = 128;
    public static final int ID_COUNT = ENTRIES_X * ENTRIES_Y;

    public static final float U_SIZE = 1f / ENTRIES_X;
    public static final float V_SIZE = 1f / ENTRIES_Y;

    private int nextIndex = 0;
    private IntSet freeIDs = new IntArraySet(ID_COUNT);
    public final Identifier atlasLocation;
    private final DynamicTexture atlasTexture;

    private boolean dirty;

    public TileAtlasManager(TextureManager textureManager) {
        this.atlasLocation = StreetArt.id("atlas");
        this.atlasTexture = new DynamicTexture(() -> "street_art:atlas", ENTRIES_X * 16, ENTRIES_Y * 16, true);
        textureManager.register(this.atlasLocation, this.atlasTexture);
    }

    public int allocateID() {
        if (this.nextIndex < ID_COUNT) {
            return this.nextIndex++;
        }
        OptionalInt id = this.freeIDs.intStream().findFirst();
        if (id.isEmpty()) {
            this.panic();
            this.nextIndex = 1;
            return 0;
        }
        this.freeIDs.remove(id.getAsInt());
        return id.getAsInt();
    }

    private void panic() {
        Minecraft.getInstance().getToastManager().addToast(new SystemToast(
                new SystemToast.SystemToastId(10000L),
                Component.translatable("street_art.toast.atlas_full.title"),
                Component.translatable("street_art.toast.atlas_full.body")
        ));
        StreetArtClient.textureManager.closeAll();
        this.clear();
    }

    public void freeID(int id) {
        this.freeIDs.add(id);
    }

    public void checkDirty() {
        if (this.dirty) {
            this.dirty = false;
            this.atlasTexture.upload();
        }
    }

    public void clear() {
        this.atlasTexture.setPixels(new NativeImage(ENTRIES_X * 16, ENTRIES_Y * 16, true));
        this.freeIDs.clear();
        this.nextIndex = 0;
    }

    public static Vector2f getUV(int id) {
        float x = id % ENTRIES_X;
        float y = id / ENTRIES_Y; // intentionally do not cast
        return new Vector2f(x / ENTRIES_X, y / ENTRIES_Y);
    }

    public void setPixel(int id, int x, int y, int color) {
        x += (id % ENTRIES_X) * 16;
        y += (id / ENTRIES_X) * 16;
        this.atlasTexture.getPixels().setPixel(x, y, color);
        this.dirty = true;
    }

    public int getPixel(int id, int x, int y) {
        x += (id % ENTRIES_X) * 16;
        y += (id / ENTRIES_X) * 16;
        return this.atlasTexture.getPixels().getPixel(x, y);
    }

    public byte[] getPixelData(int id) {
        byte[] data = new byte[16*16*4];
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int tx = (id % ENTRIES_X) * 16;
        int ty = (id / ENTRIES_X) * 16;
        for (int y = ty; y < ty + 16; y++) {
            for (int x = tx; x < tx + 16; x++) {
                buffer.putInt(this.atlasTexture.getPixels().getPixel(x, y));
            }
        }
        return data;
    }
}
