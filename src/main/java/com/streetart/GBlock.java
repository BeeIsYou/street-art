package com.streetart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Every texture plane placed on a block
 * @param <D> The data for a plane
 */
public abstract class GBlock<D extends GData> implements AutoCloseable {
    public final BlockPos blockPos;
    protected final Map<Direction, List<D>> blockData = new HashMap<>();

    public GBlock(final BlockPos pos) {
        this.blockPos = pos;
    }

    public Map<Direction, List<D>> getBlockData() {
        return this.blockData;
    }

    public static double snapToGrid(final double v) {
        return Math.round(v * 16) / 16d;
    }

    public D getOrCreate(final Direction dir, final double depth, final GManager<D, ? extends GBlock<D>> graffitiManager) {
        final List<D> dataList = this.blockData.computeIfAbsent(dir, _ -> new ArrayList<>(6));
        final double snap = snapToGrid(depth);
        for (final D data : dataList) {
            if (data.depth == snap) {
                return data;
            }
        }

        final D created = this.createData(dir, snap, this.blockPos, graffitiManager);
        dataList.add(created);
        return created;
    }

    abstract public D createData(Direction dir, double depth, BlockPos pos, GManager<D, ? extends GBlock<D>> graffitiManager);
}
