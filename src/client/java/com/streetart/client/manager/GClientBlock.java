package com.streetart.client.manager;

import com.streetart.GBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GClientBlock extends GBlock<GClientData, GClientBlock, GClientManager> {
    public GClientBlock(BlockPos pos) {
        super(pos);
    }

    @Override
    public GClientData createData(Direction dir, double depth, Vec3 pos, GClientManager manager) {
        return new GClientData(dir, depth, pos, manager.nextID(), manager.textureManager);
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
