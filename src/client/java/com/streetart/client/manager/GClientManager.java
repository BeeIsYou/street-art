package com.streetart.client.manager;

import com.streetart.ArtUtil;
import com.streetart.client.rendering.GraffitiAtlas;
import com.streetart.client.rendering.LightMath;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.GraffitiChangeData;
import com.streetart.graffiti_data.GraffitiKey;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ClientBoundInvalidateBlock;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.function.Consumer;

public class GClientManager implements AutoCloseable {
    private final GraffitiAtlas atlas;
    private final Long2ObjectMap<GClientSection> graffiti = new Long2ObjectOpenHashMap<>();

    public GClientManager(GraffitiAtlas atlas) {
        this.atlas = atlas;
    }

    public int nextID() {
        return this.atlas.allocateID();
    }

    private static long packSectionIndex(final BlockPos pos) {
        return BlockPos.asLong(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
    }

    private static AABB renderBox(final long sectionPos) {
        final int x = BlockPos.getX(sectionPos) << 4;
        final int y = BlockPos.getY(sectionPos) << 4;
        final int z = BlockPos.getZ(sectionPos) << 4;

        return new AABB(x, y, z, x + 16, y + 16, z + 16);
    }

    private @Nullable GClientSection getSection(final BlockPos pos) {
        return this.graffiti.get(packSectionIndex(pos));
    }

    private GClientSection getOrCreateSection(final BlockPos pos) {
        return this.graffiti.computeIfAbsent(packSectionIndex(pos), _ -> new GClientSection());
    }

    public @Nullable GClientBlock get(final BlockPos pos) {
        final GClientSection section = this.getSection(pos);
        if (section == null) {
            return null;
        }

        return section.get(pos);
    }

    public @Nullable GClientData get(final BlockPos pos, final Direction dir, final int depth) {
        final GClientBlock blockData = this.get(pos);
        if (blockData == null) {
            return null;
        }

        return blockData.get(dir, depth);
    }

    public GClientData getOrCreate(final BlockPos pos, final Direction dir, final int depth) {
        final GClientSection section = this.getOrCreateSection(pos);
        final GClientBlock blockData = section.computeIfAbsent(pos, _ -> this.newBlockData(pos));
        return blockData.getOrCreate(dir, depth, this);
    }

    public @Nullable GClientData getOrConditionalCreate(final BlockPos pos, final Direction dir, final int depth, final boolean clear) {
        if (clear) {
            return this.get(pos, dir, depth);
        } else {
            return this.getOrCreate(pos, dir, depth);
        }
    }

    public void tryRemoveData(final BlockPos pos, final Direction dir, final int depth) {
        final GClientBlock block = this.get(pos);
        if (block == null) {
            return;
        }

        block.remove(dir, depth);
        if (block.isEmpty(dir)) {
            block.getBlockData().remove(dir);
        }
    }

    public GClientBlock newBlockData(final BlockPos pos) {
        return new GClientBlock(this.atlas, pos);
    }

    /**
     * Will not create new data if color == 0
     *
     * @return true if pixel changed
     */
    public boolean applyPixelChange(final BlockHitResult hitResult, final Vector2i coordinates, final int color) {
        final GClientData data = this.getOrConditionalCreate(
                hitResult.getBlockPos(),
                hitResult.getDirection(),
                ArtUtil.calculateDepth(hitResult),
                color == 0
        );
        if (data == null) {
            return false;
        }
        return data.applyPixel(coordinates, color);
    }

    /**
     * Will not create new data if color == 0. Also computes light if there was no light data
     *
     * @return true if pixel changed
     */
    public boolean applyPixelChangeAndLight(final BlockHitResult hitResult, final Vector2i coordinates, final int color,
                                            final BlockAndTintGetter level) {
        final GClientData data = this.getOrConditionalCreate(
                hitResult.getBlockPos(),
                hitResult.getDirection(),
                ArtUtil.calculateDepth(hitResult),
                color == 0
        );
        if (data == null) {
            return false;
        }
        if (data.light0 == -1) {
            new LightMath().OhGodSoMuchMath(data, level, level.getBlockState(hitResult.getBlockPos()));
        }
        return data.applyPixel(coordinates, color);
    }

    public void fastForEachKey(final Consumer<BlockPos> consumer) {
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (final Long2ObjectMap.Entry<GClientSection> entry : Long2ObjectMaps.fastIterable(this.graffiti)) {
            final int x = BlockPos.getX(entry.getLongKey()) << 4;
            final int y = BlockPos.getY(entry.getLongKey()) << 4;
            final int z = BlockPos.getZ(entry.getLongKey()) << 4;

            final ShortIterator itr = entry.getValue().getRawPositions().iterator();
            while (itr.hasNext()) {
                consumer.accept(GClientSection.fromIndex(pos, x, y, z, itr.nextShort()));
            }
        }
    }

    public void forEachRender(final Consumer<GClientData> consumer, final Frustum frustum) {
        for (final Long2ObjectMap.Entry<GClientSection> section : Long2ObjectMaps.fastIterable(this.graffiti)) {
            if (!frustum.isVisible(renderBox(section.getLongKey()))) {
                continue;
            }

            for (final GClientBlock graffitis : section.getValue().getBlocks()) {
                for (final GClientData[] tiles : graffitis.getBlockData().values()) {
                    for (final GClientData tile : tiles) {
                        if (tile != null) {
                            consumer.accept(tile);
                        }
                    }
                }
            }
        }
    }

    @Deprecated(forRemoval = true)
    public void forEach(final Consumer<GClientData> consumer) {
        for (final Long2ObjectMap.Entry<GClientSection> section : Long2ObjectMaps.fastIterable(this.graffiti)) {
            for (final GClientBlock graffitis : section.getValue().getBlocks()) {
                for (final GClientData[] tiles : graffitis.getBlockData().values()) {
                    for (final GClientData tile : tiles) {
                        if (tile != null) {
                            consumer.accept(tile);
                        }
                    }
                }
            }
        }
    }

    public void handleDataUpdate(final ClientBoundGraffitiSet packet, final ClientPlayNetworking.Context context) {
        int depth = Mth.clamp(packet.depth(), 0, 15);
        if (packet.textureData().length == 16 * 16) {
            final GClientData data = this.getOrCreate(packet.pos(), packet.dir(), depth);
            data.update(packet.textureData());
            data.updateLight(context.client().level);
        } else {
            final GClientBlock block = this.get(packet.pos());
            if (block != null) {
                final GClientData[] dataList = block.getBlockData().get(packet.dir());
                if (dataList != null && dataList[depth] != null) {
                    dataList[depth].close();
                    dataList[depth] = null;
                }
            }
        }
    }

    public void handleBlockInvalidate(final ClientBoundInvalidateBlock packet, final ClientPlayNetworking.Context context) {
        final long sectionPos = packSectionIndex(packet.pos());

        final GClientSection section = this.graffiti.get(sectionPos);
        if (section == null) {
            return;
        }

        final GClientBlock block = section.remove(packet.pos());
        if (block != null) {
            block.spawnParticles(context.client().level);
            block.close();

            if (section.isEmpty()) {
                this.graffiti.remove(sectionPos);
            }
        }
    }

    public void handleChange(final byte content, final GraffitiKey key, final GraffitiChangeData change, final ClientPlayNetworking.Context context) {
        final ColorComponent colorComponent = ArtUtil.generateComponentFromByte(content);

        int depth = Mth.clamp(key.depth(), 0, 15);
        final GClientData data = this.getOrConditionalCreate(key.pos(), key.dir(), depth, colorComponent == ColorComponent.CLEAR);
        if (data != null) {
            data.handleChange(colorComponent.argb, change);
            data.updateLight(context.client().level);
        }
    }

    public void closeAll() {
        for (final GClientSection section : this.graffiti.values()) {
            section.close();
        }

        this.graffiti.clear();
    }

    @Override
    public void close() {
        this.closeAll();
    }
}
