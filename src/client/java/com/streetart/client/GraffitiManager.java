package com.streetart.client;

import com.streetart.StreetArt;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class GraffitiManager implements AutoCloseable {
    public final Int2ObjectMap<Tile> tiles = new Int2ObjectArrayMap<>();
    private final TextureManager textureManager;

    public GraffitiManager(TextureManager textureManager) {
        this.textureManager = textureManager;

        for (int i = 0; i < 5; i++) {
            int j = 0;
            for (Direction dir : Direction.values()) {
                int id = i*6 + j;
                this.tiles.put(id, new GraffitiManager.Tile(id, new Vec3(i, i, i), dir));
                j++;
            }
        }
//        this.tiles.put(0, new GraffitiManager.Tile(0, Vec3.ZERO, Direction.UP));
    }

    @Override
    public void close() throws Exception {
        for (Tile tile : this.tiles.values()) {
            tile.close();
        }
    }

    public class Tile implements AutoCloseable {
        public final int id;
        public final Vec3 pos;
        public final Direction dir;
        public final Identifier location;
        private final DynamicTexture texture;

        public Tile(int id, Vec3 pos, Direction dir) {
            this.id = id;
            this.pos = pos;
            this.dir = dir;
            this.location = StreetArt.id("graffiti/" + this.id);
            this.texture = new DynamicTexture(() -> "Graffiti " + this.id, 16, 16, true);
            GraffitiManager.this.textureManager.register(this.location, this.texture);

            int v = this.id * 15;
            int argb = 255 << 24 | v << 16 | v << 8 | v;
            this.texture.getPixels().fillRect(0, 0, 16, 16, argb);
            this.texture.upload();
        }

        @Override
        public void close() throws Exception {
            this.texture.close();
        }
    }
}
