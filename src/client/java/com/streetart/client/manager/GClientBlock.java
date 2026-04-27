package com.streetart.client.manager;

import com.streetart.client.rendering.GraffitiAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GClientBlock implements AutoCloseable {
    private final GraffitiAtlas atlas;
    public final BlockPos blockPos;
    private final Map<Direction, GClientData[]> blockData;

    public GClientBlock(final GraffitiAtlas atlas, final BlockPos pos) {
        this.atlas = atlas;
        this.blockPos = pos;
        this.blockData = new HashMap<>();
    }

    public GClientData createData(final Direction dir, final int depth, final BlockPos pos, final GClientManager manager) {
        return new GClientData(this.atlas, dir, depth, pos, manager.nextID());
    }

    @Override
    public void close() {
        this.closeAll();
    }

    public void closeAll() {
        for (final GClientData[] tiles : this.blockData.values()) {
            for (final GClientData tile : tiles) {
                if (tile != null) {
                    tile.close();
                }
            }
        }
    }

    public void forEach(final Consumer<GClientData> consumer) {
        this.blockData.forEach((_, list) -> {
            for (final GClientData data : list) {
                if (data != null) {
                    consumer.accept(data);
                }
            }
        });
    }

    public GClientData get(final Direction dir, final int depth) {
        final GClientData[] dataList = this.blockData.get(dir);
        if (dataList == null) {
            return null;
        }

        return dataList[depth];
    }

    public GClientData getOrCreate(final Direction dir, final int depth, final GClientManager manager) {
        final GClientData[] dataList = this.blockData.computeIfAbsent(dir, _ -> new GClientData[15]);
        if (dataList[depth] == null) {
            dataList[depth] = this.createData(dir, depth, this.blockPos, manager);
        }

        return dataList[depth];
    }

    public void remove(final Direction dir, final int depth) {
        final GClientData[] dataList = this.blockData.get(dir);
        if (dataList == null) {
            return;
        }

        if (dataList[depth] != null) {
            dataList[depth].close();
            dataList[depth] = null;
        }
    }

    // todo jomlfy this
    public void spawnParticles(final Level level) {
        for (final Map.Entry<Direction, GClientData[]> value : this.blockData.entrySet()) {
            final Direction dir = value.getKey();
            final Vec3 pos = Vec3.atLowerCornerOf(this.blockPos);
            final Vec3 dx = switch (dir.getAxis()) {
                case X -> Vec3.Y_AXIS;
                case Y -> Vec3.Z_AXIS;
                case Z -> Vec3.X_AXIS;
            };
            final Vec3 dy = switch (dir.getAxis()) {
                case X -> Vec3.Z_AXIS;
                case Y -> Vec3.X_AXIS;
                case Z -> Vec3.Y_AXIS;
            };

            for (final GClientData tile : value.getValue()) {
                if (tile != null) {
                    final Vec3 tilePos;
                    if (dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                        tilePos = pos.add(dir.getUnitVec3().scale(tile.depth / 16d + 0.1));
                    } else {
                        tilePos = pos.add(dir.getUnitVec3().scale(1.1 - tile.depth / 16d));
                    }

                    for (int i = 0; i < 15; i++) {
                        final int px = level.getRandom().nextInt(16);
                        final int py = level.getRandom().nextInt(16);
                        final int color = tile.getPixel(px, py);
                        if (color != 0) {
                            final Vec3 particlePos = tilePos.add(dx.scale(px / 16f)).add(dy.scale(py / 16f));
                            level.addParticle(new DustParticleOptions(color, 1),
                                    particlePos.x, particlePos.y, particlePos.z, 0, 0, 0
                            );
                        }
                    }
                }
            }
        }
    }

    public Map<Direction, GClientData[]> getBlockData() {
        return this.blockData;
    }

    public boolean isEmpty(final Direction dir) {
        final GClientData[] dataList = this.blockData.get(dir);
        if (dataList == null) {
            return true;
        }

        for (final GClientData data : dataList) {
            if (data != null) {
                return false;
            }
        }

        return true;
    }
}
