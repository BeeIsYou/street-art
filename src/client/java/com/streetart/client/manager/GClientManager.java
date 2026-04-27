package com.streetart.client.manager;

import com.streetart.ArtUtil;
import com.streetart.client.rendering.GraffitiAtlas;
import com.streetart.client.rendering.LightMath;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.GraffitiChangeData;
import com.streetart.graffiti_data.GraffitiKey;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ClientBoundInvalidateBlock;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GClientManager implements AutoCloseable {
    private final GraffitiAtlas atlas;
    private final Map<BlockPos, GClientBlock> graffiti = new HashMap<>();

    public GClientManager(GraffitiAtlas atlas) {
        this.atlas = atlas;
    }

    public int nextID() {
        return this.atlas.allocateID();
    }

    public @Nullable GClientBlock get(final BlockPos pos) {
        return this.getGraffiti().get(pos);
    }

    public @Nullable GClientData get(final BlockPos pos, final Direction dir, final int depth) {
        final GClientBlock blockData = this.getGraffiti().get(pos);
        if (blockData == null) {
            return null;
        }

        return blockData.get(dir, depth);
    }

    public GClientData getOrCreate(final BlockPos pos, final Direction dir, final int depth) {
        final GClientBlock blockData = this.getGraffiti().computeIfAbsent(pos, _ -> this.newBlockData(pos));
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
        final GClientBlock block = this.getGraffiti().get(pos);
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

    public Map<BlockPos, GClientBlock> getGraffiti() {
        return this.graffiti;
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

    public void forEach(final Consumer<GClientData> consumer) {
        for (final GClientBlock graffitis : this.getGraffiti().values()) {
            for (final Map.Entry<Direction, GClientData[]> tiles : graffitis.getBlockData().entrySet()) {
                for (final GClientData tile : tiles.getValue()) {
                    if (tile != null) {
                        consumer.accept(tile);
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
            final GClientBlock block = this.getGraffiti().get(packet.pos());
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
        final GClientBlock block = this.getGraffiti().remove(packet.pos());
        if (block != null) {
            block.spawnParticles(context.client().level);
            block.close();
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
        for (final GClientBlock graffitis : this.getGraffiti().values()) {
            graffitis.close();
        }

        this.graffiti.clear();
    }

    @Override
    public void close() {
        this.closeAll();
    }
}
