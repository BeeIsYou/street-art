package com.streetart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Level-wide manager for graffiti instances
 *
 * @param <D> The data for a plane
 * @param <B> All the planes on a block
 */
public abstract class GManager<D extends GData, B extends GBlock<D>> implements AutoCloseable {

    public @Nullable D get(final BlockPos pos, final Direction dir, final double depth) {
        final B blockData = this.getGraffiti().get(pos);
        if (blockData == null) {
            return null;
        }

        return blockData.get(dir, depth);
    }

    public D getOrCreate(final BlockPos pos, final Direction dir, final double depth) {
        final B blockData = this.getGraffiti().computeIfAbsent(pos, _ -> this.newBlockData(pos));
        return blockData.getOrCreate(dir, depth, this);
    }

    public @Nullable D getOrConditionalCreate(final BlockPos pos, final Direction dir, final double depth, final boolean clear) {
        if (clear) {
            return this.get(pos, dir, depth);
        } else {
            return this.getOrCreate(pos, dir, depth);
        }
    }

    public void tryRemoveData(final BlockPos pos, final Direction dir, final double depth) {
        final B block = this.getGraffiti().get(pos);
        if (block == null) {
            return;
        }

        block.remove(dir, depth);
        if (block.getBlockData().get(dir).isEmpty()) {
            block.getBlockData().remove(dir);
        }
    }

    public @Nullable B popBlock(final BlockPos pos) {
        return this.getGraffiti().remove(pos);
    }

    public abstract B newBlockData(BlockPos pos);

    public abstract Map<BlockPos, B> getGraffiti();
}
