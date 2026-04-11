package com.streetart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class GBlock<
            D extends GData,
            B extends GBlock<D, B, M>,
            M extends GManager<D, B, M>
        > implements AutoCloseable {
    protected final BlockPos blockPos;
    protected final Map<Direction, List<D>> blockData = new HashMap<>();

    public GBlock(BlockPos pos) {
        this.blockPos = pos;
    }

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
        Vec3 pos = new Vec3(
                this.blockPos.getX() + dir.getStepX() * depth,
                this.blockPos.getY() + dir.getStepY() * depth,
                this.blockPos.getZ() + dir.getStepZ() * depth
        );
        D created = this.createData(dir, depth, pos, graffitiManager);
        dataList.add(created);
        return created;
    }

    abstract public D createData(Direction dir, double depth, Vec3 pos, M graffitiManager);
}
