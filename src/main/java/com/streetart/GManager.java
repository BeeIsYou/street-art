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

    public D getOrCreate(final BlockPos pos, final Direction dir, final double depth) {
        final B blockData = this.getGraffiti().computeIfAbsent(pos, _ -> this.newBlockData(pos));
        return blockData.getOrCreate(dir, depth, this);
    }

    public @Nullable B popBlock(final BlockPos pos) {
        return this.getGraffiti().remove(pos);
    }

    public abstract B newBlockData(BlockPos pos);

    public abstract Map<BlockPos, B> getGraffiti();
}
