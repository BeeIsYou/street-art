package com.streetart.managers;

import com.streetart.GBlock;

public class GServerBlock extends GBlock<GServerData, GServerBlock, GLevelManager> {
    @Override
    public GServerData createData(double depth, GLevelManager graffitiManager) {
        return new GServerData(depth);
    }

    @Override
    public void close() {}
}
