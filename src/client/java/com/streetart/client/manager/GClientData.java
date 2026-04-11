package com.streetart.client.manager;

import com.google.common.primitives.Ints;
import com.streetart.GData;
import com.streetart.StreetArt;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

public class GClientData extends GData implements AutoCloseable {
    public final int id;
    public final Identifier location;
    private final DynamicTexture texture;

    public int light = 0;

    public GClientData(double depth, int id, TextureManager textureManager) {
        super(depth);
        this.id = id;
        this.location = StreetArt.id("graffiti/" + this.id);
        this.texture = new DynamicTexture(() -> "Graffiti " + this.id, 16, 16, true);
        textureManager.register(this.location, this.texture);
    }

    public void update(byte[] data) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int i = (x + y*16) * 4;
                int argb = Ints.fromBytes(data[i], data[i+1], data[i+2], data[i+3]);
                this.texture.getPixels().setPixel(x, y, argb);
            }
        }
        this.texture.upload();
    }

    @Override
    public void close() {
        this.texture.close();
    }
}
