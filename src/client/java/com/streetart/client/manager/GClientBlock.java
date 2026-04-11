package com.streetart.client.manager;

import com.streetart.GBlock;
import com.streetart.GManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public class GClientBlock extends GBlock<GClientData> {
    public GClientBlock(final BlockPos pos) {
        super(pos);
    }

    @Override
    public GClientData createData(final Direction dir, final double depth, final BlockPos pos, final GManager<GClientData, ? extends GBlock<GClientData>> graffitiManager) {
        //cast to client
        assert graffitiManager instanceof GClientManager;
        final GClientManager clientManager = (GClientManager) graffitiManager;

        return new GClientData(dir, depth, pos, clientManager.nextID(), clientManager.textureManager);
    }

    @Override
    public void close() {
        this.closeAll();
    }

    public void closeAll() {
        for (final List<GClientData> tiles : this.blockData.values()) {
            for (final GClientData tile : tiles) {
                tile.close();
            }
        }
    }
}
