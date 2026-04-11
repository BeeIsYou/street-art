package com.streetart.managers;

import com.streetart.GBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class GServerBlock extends GBlock<GServerData, GServerBlock, GLevelManager> {
    public GServerBlock(BlockPos pos) {
        super(pos);
    }

    @Override
    public void close() {}

    @Override
    public GServerData createData(Direction dir, double depth, Vec3 pos, GLevelManager graffitiManager) {
        return new GServerData(depth);
    }
}
