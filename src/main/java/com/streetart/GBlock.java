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
    private final BlockPos blockPos;
    private final Map<Direction, List<D>> blockData;

    public GBlock(final BlockPos pos) {
        this.blockPos = pos;
        this.blockData = new HashMap<>();
    }

    public GBlock(final Map<Direction, List<D>> blockData, final BlockPos pos) {
        this.blockPos = pos;
        this.blockData = blockData;
    }

    public Map<Direction, List<D>> getBlockData() {
        return this.blockData;
    }

    public static double snapToGrid(final double v) {
        return Math.round(v * 16) / 16d;
    }

    public D getOrCreate(final Direction dir, final double depth, final GManager<D, ? extends GBlock<D>> graffitiManager) {
        final List<D> dataList = this.getBlockData().computeIfAbsent(dir, _ -> new ArrayList<>(6));
        final double snap = snapToGrid(depth);
        for (final D data : dataList) {
            if (data.getDepth() == snap) {
                return data;
            }
        }

        final D created = this.createData(dir, snap, this.getBlockPos(), graffitiManager);
        dataList.add(created);
        return created;
    }

    abstract public D createData(Direction dir, double depth, BlockPos pos, GManager<D, ? extends GBlock<D>> graffitiManager);

    public BlockPos getBlockPos() {
        return this.blockPos;
    }
}
