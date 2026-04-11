package com.streetart;

import net.minecraft.core.Direction;

import java.util.*;

public abstract class GBlock<
            D extends GData,
            B extends GBlock<D, B, M>,
            M extends GManager<D, B, M>
        > implements AutoCloseable {
    protected final Map<Direction, List<D>> blockData = new HashMap<>();

    public Set<Map.Entry<Direction, List<D>>> entrySet() {
        return this.blockData.entrySet();
    }

    public static double snapToGrid(double v) {
        return Math.round(v * 16) / 16d;
    }

    public D getOrCreate(Direction dir, double depth, M graffitiManager) {
        List<D> dataList = this.blockData.computeIfAbsent(dir, _ -> new ArrayList<>(6));
        for (D data : dataList) {
            double snap = snapToGrid(depth);
            if (data.depth == snap) {
                return data;
            }
        }
        D created = this.createData(depth, graffitiManager);
        dataList.add(created);
        return created;
    }

    abstract public D createData(double depth, M graffitiManager);
}
