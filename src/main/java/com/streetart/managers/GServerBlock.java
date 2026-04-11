package com.streetart.managers;

import com.streetart.GBlock;
import com.streetart.GManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class GServerBlock extends GBlock<GServerData> {
    public GServerBlock(final BlockPos pos) {
        super(pos);
    }

    @Override
    public GServerData createData(final Direction dir, final double depth, final BlockPos pos, final GManager<GServerData, ? extends GBlock<GServerData>> graffitiManager) {
        return new GServerData(depth);
    }

    @Override
    public void close() {}

}
