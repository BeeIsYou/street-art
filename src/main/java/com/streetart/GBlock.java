package com.streetart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GBlock<
            D extends GData,
            B extends GBlock<D, B, M>,
            M extends GManager<D, B, M>
        > implements AutoCloseable {
    public final BlockPos blockPos;
    protected final Map<Direction, List<D>> blockData = new HashMap<>();

    public GBlock(BlockPos pos) {
        this.blockPos = pos;
    }

    public Map<Direction, List<D>> getBlockData() {
        return this.blockData;
    }

    public static double snapToGrid(double v) {
        return Math.round(v * 16) / 16d;
    }

    public D getOrCreate(Direction dir, double depth, M graffitiManager) {
        List<D> dataList = this.blockData.computeIfAbsent(dir, _ -> new ArrayList<>(6));
        double snap = snapToGrid(depth);
        for (D data : dataList) {
            if (data.depth == snap) {
                return data;
            }
        }

        D created = this.createData(dir, snap, this.blockPos, graffitiManager);
        dataList.add(created);
        return created;
    }

    abstract public D createData(Direction dir, double depth, BlockPos pos, M graffitiManager);
}
