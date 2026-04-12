package com.streetart.client.manager;

import com.streetart.GBlock;
import com.streetart.GManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class GClientBlock extends GBlock<GClientData> {
    public GClientBlock(final BlockPos pos) {
        super(pos);
    }

    @Override
    public GClientData createData(final Direction dir, final double depth, final BlockPos pos, final GManager<GClientData, ? extends GBlock<GClientData>> graffitiManager) {
        //cast to client
        assert graffitiManager instanceof GClientManager;
        final GClientManager clientManager = (GClientManager) graffitiManager;

        return new GClientData(dir, depth, pos, clientManager.nextID());
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

    // todo jomlfy this
    public void spawnParticles(Level level) {
        for (Map.Entry<Direction, List<GClientData>> value : this.getBlockData().entrySet()) {
            Direction dir = value.getKey();
            Vec3 pos = Vec3.atLowerCornerOf(this.blockPos);
            Vec3 dx = switch (dir.getAxis()) {
                case X -> Vec3.Y_AXIS;
                case Y -> Vec3.Z_AXIS;
                case Z -> Vec3.X_AXIS;
            };
            Vec3 dy = switch (dir.getAxis()) {
                case X -> Vec3.Z_AXIS;
                case Y -> Vec3.X_AXIS;
                case Z -> Vec3.Y_AXIS;
            };

            for (final GClientData tile : value.getValue()) {
                Vec3 tilePos;
                if (dir.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    tilePos = pos.add(dir.getUnitVec3().scale(tile.depth + 0.1));
                } else {
                    tilePos = pos.add(dir.getUnitVec3().scale(0.9 - tile.depth));
                }

                for (int i = 0; i < 15; i++) {
                    int px = level.getRandom().nextInt(16);
                    int py = level.getRandom().nextInt(16);
                    int color = tile.getPixel(px, py);
                    if (color != 0) {
                        Vec3 particlePos = tilePos.add(dx.scale(px / 16f)).add(dy.scale(py / 16f));
                        level.addParticle(new DustParticleOptions(color, 1),
                                particlePos.x, particlePos.y, particlePos.z, 0, 0, 0
                        );
                    }
                }
            }
        }
    }
}
