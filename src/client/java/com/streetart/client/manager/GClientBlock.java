package com.streetart.client.manager;

import com.streetart.GBlock;

import java.util.List;

public class GClientBlock extends GBlock<GClientData, GClientBlock, GClientManager> {
    @Override
    public GClientData createData(double depth, GClientManager manager) {
        return new GClientData(depth, manager.nextID(), manager.textureManager);
    }

    @Override
    public void close() {
        this.closeAll();
    }

    public void closeAll() {
        for (List<GClientData> tiles : this.blockData.values()) {
            for (GClientData tile : tiles) {
                tile.close();
            }
        }
    }
}
