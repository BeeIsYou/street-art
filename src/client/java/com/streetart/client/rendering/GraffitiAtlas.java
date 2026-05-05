package com.streetart.client.rendering;

import com.mojang.blaze3d.platform.NativeImage;
import com.streetart.StreetArt;
import com.streetart.StreetArtConfig;
import com.streetart.client.manager.GClientManager;
import com.streetart.graffiti_data.GraffitiChangeData;
import com.streetart.graffiti_data.GraffitiKey;
import com.streetart.graffiti_data.GraffitiLayerType;
import com.streetart.networking.BiDirectionalGraffitiChange;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ClientBoundInvalidateBlock;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class GraffitiAtlas {
    private final TextureManager textureManager;
    private final BiPredicate<Player, Level> visibility;
    public final int renderingPriority;
    private final Map<ChunkPos, GClientManager> graffitiChunks;
    public final Identifier layer;
    private DynamicMippedTexture atlasTexture;
    private final int mipCount;

    private int entriesX = 128;
    private int entriesY = 128;
    private int idCount = this.entriesX * this.entriesY;

    private float uSize = 1f / this.entriesX;
    private float vSize = 1f / this.entriesY;

    private int nextIndex = 0;
    private int useCount = 0;
    private final IntSet freeIDs = new IntOpenHashSet(this.idCount);
    private final IntSet toReMip = new IntOpenHashSet(this.idCount);


    private boolean dirty;

    // todo respect mipmap setting ?
    public GraffitiAtlas(final TextureManager textureManager, final GraffitiLayerType layer, final int mipLevels) {
        this.textureManager = textureManager;
        this.graffitiChunks = new HashMap<>();
        this.layer = layer.identifier();
        this.visibility = layer.visibility();
        this.renderingPriority = layer.renderingPriority();
        this.mipCount = mipLevels + 1;

        this.atlasTexture = new DynamicMippedTexture(this.layer::toString,
                this.entriesX * 16, this.entriesY * 16,
                true, this.mipCount);
        this.textureManager.register(this.layer, this.atlasTexture);
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
        final DynamicMippedTexture newTexture = new DynamicMippedTexture(
                this.layer::toString,
                this.entriesX * 32, this.entriesY * 32,
                true, this.mipCount);
        final NativeImage[] newPix = newTexture.getPixels();
        final NativeImage[] oldPix = this.atlasTexture.getPixels();
        // puts textures into the atlas in such a way that an id for NxN will point to the same block of data in 2Nx2N
        int res = 16;
        for (int i = 0; i < newPix.length; i++) {
            for (int y = 0; y < this.entriesY; y++) {
                if (y % 2 == 0) {
                    final int ny = y / 2;
                    for (int py = 0; py < res; py++) {
                        for (int px = 0; px < this.entriesX * res; px++) {
                            newPix[i].setPixel(
                                    px,
                                    ny * res + py,
                                    oldPix[i].getPixel(px, y * res + py)
                            );
                        }
                    }
                } else {
                    final int ny = (y - 1) / 2;
                    for (int py = 0; py < res; py++) {
                        for (int px = 0; px < this.entriesX * res; px++) {
                            newPix[i].setPixel(
                                    px + this.entriesX * res,
                                    ny * res + py,
                                    oldPix[i].getPixel(px, y * res + py)
                            );
                        }
                    }
                }
            }
            res = res >> 1;
        }

        this.atlasTexture.close();
        this.atlasTexture = newTexture;
        this.textureManager.register(this.layer, this.atlasTexture);

        this.entriesX *= 2;
        this.entriesY *= 2;
        this.idCount *= 4;
        this.uSize /= 2;
        this.vSize /= 2;

        StreetArt.LOGGER.info("Resizing spray paint atlas to {}x{} ({} entries)", this.entriesX * 16, this.entriesY * 16, this.entriesX * this.entriesY);
    }

    public void freeID(final int id) {
        this.freeIDs.add(id);
        this.useCount--;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                this.setPixel(id, x, y, 0);
            }
        }
    }

    public void checkDirty() {
        if (this.dirty) {
            this.dirty = false;
            for (final int id : this.toReMip) {
                this.reMip(id);
            }
            this.toReMip.clear();
            this.atlasTexture.upload();
        }
    }

    private void reMip(final int id) {
        final NativeImage[] images = this.atlasTexture.getPixels();
        NativeImage base = images[0];
        int idx = (id % this.entriesX) * 8;
        int idy = (id / this.entriesX) * 8;
        int l = 8;
        final int[] colors = new int[4];
        for (int i = 1; i < images.length; i++) {
            final NativeImage target = images[i];
            for (int y = idy; y < idy + l; y++) {
                for (int x = idx; x < idx + l; x++) {
                    colors[0] = base.getPixel(x * 2    , y * 2    );
                    colors[1] = base.getPixel(x * 2 + 1, y * 2    );
                    colors[2] = base.getPixel(x * 2 + 1, y * 2 + 1);
                    colors[3] = base.getPixel(x * 2    , y * 2 + 1);

                    int nonTransparent = 0;
                    int a = 0;
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    for (final int color : colors) {
                        final int ca = (color & 0xFF000000) >>> 24;
                        if (ca != 0) {
                            nonTransparent++;
                            a += ca;
                            r += (color & 0x00FF0000) >> 16;
                            g += (color & 0x0000FF00) >> 8;
                            b += (color & 0x000000FF);
                        }
                    }

                    int color = 0;
                    if (nonTransparent > 0) {
                        // alpha respecting mix that doesn't darken at the edges
                        a /= colors.length;
                        r /= nonTransparent;
                        g /= nonTransparent;
                        b /= nonTransparent;
                        color = a << 24 | r << 16 | g << 8 | b;
                    }

                    target.setPixel(x, y, color);
                }
            }

            base = target;
            l = l >> 1;
            idx = idx >> 1;
            idy = idy >> 1;
        }
    }

    public void clear() {
        for (final NativeImage image : this.atlasTexture.getPixels()) {
            image.fillRect(0, 0, image.getWidth(), image.getHeight(), 0);
        }
        this.dirty = true;
        for (GClientManager manager : this.graffitiChunks.values()) {
            manager.closeAll();
        }
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
    private static int SEED = new Random().nextInt();

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

        this.toReMip.add(id);

        for (int i = 0; i < this.atlasTexture.getPixels().length; i++) {
            this.atlasTexture.getPixels()[i].setPixel(x >> i, y >> i, color);
        }
        this.atlasTexture.getBasePixels().setPixel(x, y, color);
        this.dirty = true;
    }

    public int getBasePixel(final int id, int x, int y) {
        x += (id % this.entriesX) * 16;
        y += (id / this.entriesX) * 16;
        return this.atlasTexture.getBasePixels().getPixel(x, y);
    }

    public byte[] copyBasePixelData(final int id) {
        final byte[] data = new byte[16*16*4];
        final ByteBuffer buffer = ByteBuffer.wrap(data);

        final int tx = (id % this.entriesX) * 16;
        final int ty = (id / this.entriesX) * 16;
        for (int y = ty; y < ty + 16; y++) {
            for (int x = tx; x < tx + 16; x++) {
                buffer.putInt(this.atlasTexture.getBasePixels().getPixel(x, y));
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

    public GClientManager get(final BlockPos pos) {
        return this.graffitiChunks.get(ChunkPos.containing(pos));
    }

    public GClientManager getOrCreate(final BlockPos pos) {
        return this.graffitiChunks.computeIfAbsent(ChunkPos.containing(pos), _ -> new GClientManager(this));
    }

    public void handleSetPacket(final ClientBoundGraffitiSet packet, final ClientPlayNetworking.Context context) {
        if (!StreetArtConfig.ignoreEverything()) {
            this.getOrCreate(packet.pos()).handleDataUpdate(packet, context);
        }
    }

    public void handleInvalidatePacket(final ClientBoundInvalidateBlock packet, final ClientPlayNetworking.Context context) {
        if (!StreetArtConfig.ignoreEverything()) {
            this.getOrCreate(packet.pos()).handleBlockInvalidate(packet, context);
        }
    }

    public void handleChangePacket(final BiDirectionalGraffitiChange packet, final ClientPlayNetworking.Context context) {
        if (!StreetArtConfig.ignoreEverything()) {
            for (final Map.Entry<GraffitiKey, GraffitiChangeData> change : packet.changes().entrySet()) {
                this.getOrCreate(change.getKey().pos()).handleChange(
                        packet.content(),
                        change.getKey(),
                        change.getValue(),
                        context
                );
            }
        }
    }

    public void checkClear(final Minecraft minecraft) {
        if (minecraft.level == null) {
            // todo do not do this every tick
            this.clear();
        }
    }

    public void closeAll() {
        for (final GClientManager manager : this.graffitiChunks.values()) {
            manager.closeAll();
        }
        this.graffitiChunks.clear();
    }

    public void handleChunkUnload(final LevelChunk chunk) {
        final GClientManager manager = this.graffitiChunks.get(chunk.getPos());
        if (manager != null) {
            manager.closeAll();
        }
    }

    public void forEach(final BiConsumer<ChunkPos, GClientManager> consumer) {
        this.graffitiChunks.forEach(consumer);
    }

    public boolean isActive(final Player player, final Level level) {
        return this.visibility.test(player, level);
    }
}
