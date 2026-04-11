package com.streetart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Level-wide manager for graffiti instances
 * @param <D> The data for a plane
 * @param <B> All the planes on a block
 * @param <M> This. Because generic hell reasons
 */
public abstract class GManager<
            D extends GData,
            B extends GBlock<D, B, M>,
            M extends GManager<D, B, M>
        > implements AutoCloseable {

    public D getOrCreate(BlockPos pos, Direction dir, double depth) {
        B blockData = this.getGraffiti().computeIfAbsent(pos, _ -> this.newBlockData(pos));
        return blockData.getOrCreate(dir, depth, (M) this);
    }

    public D getOrCreate(BlockPos pos, Direction dir, Vec3 absolutePos) {
        final Vec3 relativePos = absolutePos.subtract(Vec3.atLowerCornerOf(pos));
        double depth = switch (dir.getAxis()) {
            case X -> relativePos.x;
            case Y -> relativePos.y;
            case Z -> relativePos.z;
        };

        if (dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            depth = 1 - depth;
        }

        return this.getOrCreate(pos, dir, depth);
    }

    public @Nullable B popBlock(BlockPos pos) {
        return this.getGraffiti().remove(pos);
    }

    public abstract B newBlockData(BlockPos pos);

    public abstract Map<BlockPos, B> getGraffiti();
}
