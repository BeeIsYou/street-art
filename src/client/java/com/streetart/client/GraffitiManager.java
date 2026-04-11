package com.streetart.client;

import com.streetart.StreetArt;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GraffitiManager implements AutoCloseable {
    private final Map<BlockPos, BlockGraffiti> graffiti = new HashMap<>();
    private final TextureManager textureManager;
    private int id = 0;

    public GraffitiManager(TextureManager textureManager) {
        this.textureManager = textureManager;

//        for (int i = 0; i < 5; i++) {
//            BlockPos p = new BlockPos(i, i, i);
//            for (Direction dir : Direction.values()) {
//                this.getOrNew(p, dir.getUnitVec3(), dir);
//            }
//        }
    }

    public Tile getOrNew(BlockPos block, Vec3 hitPos, Direction dir) {
        hitPos = hitPos.subtract(block.getX(), block.getY(), block.getZ());
        BlockGraffiti graffitis = this.graffiti.computeIfAbsent(block, _ -> new BlockGraffiti());
        return graffitis.getOrNew(hitPos, dir);
    }

    public Tile getOrNew(BlockPos block, double depth, Direction dir) {
        BlockGraffiti graffitis = this.graffiti.computeIfAbsent(block, _ -> new BlockGraffiti());
        return graffitis.getOrNew(depth, dir);
    }

    public void forEach(Consumer<TileData> consumer) {
        TileData data = new TileData();
        for (Map.Entry<BlockPos, BlockGraffiti> graffitis : this.graffiti.entrySet()) {
            BlockPos pos = graffitis.getKey();
            for (Map.Entry<Direction, List<Tile>> tiles : graffitis.getValue().blockTiles.entrySet()) {
                data.dir = tiles.getKey();
                for (Tile tile : tiles.getValue()) {
                    data.pos.set(pos.getX(), pos.getY(), pos.getZ());
                    data.pos.fma(tile.depth + 0.01, data.dir.getUnitVec3f());
                    data.tile = tile;
                    consumer.accept(data);
                }
            }
        }
    }

    public void closeAll() {
        for (BlockGraffiti graffitis : this.graffiti.values()) {
            graffitis.close();
        }
        this.graffiti.clear();
    }

    @Override
    public void close() throws Exception {
        this.closeAll();
    }

    public class BlockGraffiti implements AutoCloseable {
        Map<Direction, List<Tile>> blockTiles = new HashMap<>();

        public Tile getOrNew(double depth, Direction dir) {
            List<Tile> tiles = this.blockTiles.computeIfAbsent(dir, _ -> new ArrayList<>());
            for (Tile tile : tiles) {
                if (Math.abs(tile.depth - depth) < 1E-4) {
                    return tile;
                }
            }
            Tile created = new Tile(depth);
            tiles.add(created);
            return created;
        }

        public Tile getOrNew(Vec3 relativePos, Direction dir) {
            double depth = switch (dir.getAxis()) {
                case X -> relativePos.x;
                case Y -> relativePos.y;
                case Z -> relativePos.z;
            };
            if (dir.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                depth -= 1;
            }
            return this.getOrNew(depth, dir);
        }

        public void closeAll() {
            for (List<Tile> tiles : this.blockTiles.values()) {
                for (Tile tile : tiles) {
                    try {
                        tile.close();
                    } catch (Exception _) {}
                }
            }
        }

        @Override
        public void close() {
            this.closeAll();
        }
    }

    public class Tile implements AutoCloseable {
        public final int id;
        public final double depth;
        public final Identifier location;
        private final DynamicTexture texture;

        public Tile(double depth) {
            this.id = GraffitiManager.this.id++;
            this.depth = depth;
            this.location = StreetArt.id("graffiti/" + this.id);
            this.texture = new DynamicTexture(() -> "Graffiti " + this.id, 16, 16, true);
            GraffitiManager.this.textureManager.register(this.location, this.texture);

            int v = this.id * 15;
            int argb = 255 << 24 | v << 16 | v << 8 | v;
            this.texture.getPixels().fillRect(0, 0, 16, 16, argb);
            this.texture.upload();
        }

        @Override
        public void close() throws Exception {
            this.texture.close();
        }
    }

    public static class TileData {
        public Vector3d pos = new Vector3d();
        public Direction dir;
        public Tile tile;
    }
}
