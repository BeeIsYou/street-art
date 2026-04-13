package com.streetart;

import com.streetart.graffiti_data.TileKey;
import com.streetart.managers.GServerChunkManager;
import com.streetart.managers.GServerDataHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.util.*;
import java.util.function.BiConsumer;

public class ArtUtil {
    public static Vector2i calculatePixelCoordinates(final BlockHitResult hitResult) {
        final BlockPos pos = hitResult.getBlockPos();
        final Vec3 clickPosition = hitResult.getLocation();
        final Vector3f relative = new Vector3f(
                (float)(clickPosition.x - pos.getX()),
                (float)(clickPosition.y - pos.getY()),
                (float)(clickPosition.z - pos.getZ())
        );
        final Vec2 plane = switch (hitResult.getDirection()) {
            case DOWN -> new Vec2(relative.x, 1-relative.z);
            case UP -> new Vec2(relative.x, relative.z);
            case NORTH -> new Vec2(1-relative.x, 1-relative.y);
            case SOUTH -> new Vec2(relative.x, 1-relative.y);
            case WEST -> new Vec2(relative.z, 1-relative.y);
            case EAST -> new Vec2(1-relative.z, 1-relative.y);
        };
        final int x = (int)(plane.x * 16);
        final int y = (int)(plane.y * 16);
        return new Vector2i(x, y);
    }

    public static double calculateDepth(final BlockHitResult hitResult) {
        final Vec3 relativePos = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(hitResult.getBlockPos()));
        double depth = switch (hitResult.getDirection().getAxis()) {
            case X -> relativePos.x;
            case Y -> relativePos.y;
            case Z -> relativePos.z;
        };

        if (hitResult.getDirection().getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            depth = 1 - depth;
        }
        return depth;
    }

    public static List<ShapeFaces> doThingsWithVoxelShape(final VoxelShape shape) {
        final List<ShapeFaces> faces = new ArrayList<>();
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            final int ix1 = Mth.floor(x1 * 16);
            final int iy1 = Mth.floor(y1 * 16);
            final int iz1 = Mth.floor(z1 * 16);
            final int ix2 = Mth.ceil(x2 * 16);
            final int iy2 = Mth.ceil(y2 * 16);
            final int iz2 = Mth.ceil(z2 * 16);
            faces.add(new ShapeFaces(
                    new Face(y2, ix1, iz1, ix2, iz2),
                    new Face(1 - y1, ix1, 16 - iz2, ix2, 16 - iz1),
                    new Face(1 - z1, 16 - ix2, 16 - iy2, 16 - ix1, 16 - iy1),
                    new Face(x2, 16 - iz2, 16 - iy2, 16 - iz1, 16 - iy1),
                    new Face(z2, ix1, 16 - iy2, ix2, 16 - iy1),
                    new Face(1 - x1, iz1, 16 - iy2, iz2, 16 - iy1)
            ));
        });
        return faces;
    }

    public static void paintExplosion(final ServerLevel serverLevel, final Vec3 origin, final int color) {
        // BlockPos -> Direction -> # of rays hit
        final HashMap<BlockPos, EnumMap<Direction, Integer>> affectedBlocks = new HashMap<>();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    // outside surface of a cube
                    if (i == 0 || i == 15 || j == 0 || j == 15 || k == 0 || k == 15) {
                        final Vec3 dirVec = new Vec3(
                                i / 15.0F * 2.0F - 1.0F,
                                j / 15.0F * 2.0F - 1.0F,
                                k / 15.0F * 2.0F - 1.0F
                        ).normalize().scale(3);

                        final BlockHitResult result = serverLevel.clip(new ClipContext(
                                origin,
                                origin.add(dirVec),
                                ClipContext.Block.OUTLINE,
                                ClipContext.Fluid.NONE,
                                CollisionContext.empty())
                        );
                        if (result.getType() == HitResult.Type.BLOCK) {
                            affectedBlocks.computeIfAbsent(result.getBlockPos(), _ -> new EnumMap<>(Direction.class))
                                    .compute(result.getDirection(), (_, c) -> c == null ? 1 : c + 1);
                        }
                    }
                }
            }
        }

        // 16*16*16 - 14*14*14 = 1352 rays
        for (final Map.Entry<BlockPos, EnumMap<Direction, Integer>> blockEntry : affectedBlocks.entrySet()) {
            final BlockPos pos = blockEntry.getKey();
            final List<ShapeFaces> faces = doThingsWithVoxelShape(serverLevel.getBlockState(pos).getShape(serverLevel, pos));
            blockEntry.getValue().forEach((dir, hits) -> {
                final Vector4i hitsGradient = getHitsGradient(affectedBlocks, pos, dir);
                latherInPaint(serverLevel, faces, pos, dir, color, hitsGradient);
            });
        }
    }

    private static Vector4i getHitsGradient(final HashMap<BlockPos, EnumMap<Direction, Integer>> affectedBlocks,
                                            final BlockPos pos, final Direction dir
    ) {
        final int[] parts = new int[9];
        final Direction dx = switch (dir) {
            case DOWN -> Direction.WEST;
            case UP -> Direction.WEST;
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            case EAST -> Direction.SOUTH;
        };
        final Direction dy = switch (dir) {
            case DOWN -> Direction.SOUTH;
            case UP -> Direction.NORTH;
            case NORTH -> Direction.UP;
            case SOUTH -> Direction.UP;
            case WEST -> Direction.UP;
            case EAST -> Direction.UP;
        };
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                final BlockPos offPos = pos.relative(dx, i).relative(dy, j);
                final EnumMap<Direction, Integer> exposures = affectedBlocks.get(offPos);
                if (exposures != null) {
                    parts[i+j*3+4] = exposures.getOrDefault(dir, 0);
                } else {
                    parts[i+j*3+4] = 0;
                }
            }
        }
        return new Vector4i(
                parts[0] + parts[1] + parts[3] + parts[4],
                parts[1] + parts[2] + parts[4] + parts[5],
                parts[3] + parts[4] + parts[6] + parts[7],
                parts[4] + parts[5] + parts[7] + parts[8]
        );
    }

    public static void latherInPaint(final ServerLevel serverLevel, final BlockPos pos, final int color) {
        final BlockState block = serverLevel.getBlockState(pos);
        final List<ShapeFaces> shapeFaces = ArtUtil.doThingsWithVoxelShape(block.getShape(serverLevel, pos));
        final ChunkAccess chunk = serverLevel.getChunk(pos);
        final GServerChunkManager manager = chunk.getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER);
        final boolean[] unsmothered = new boolean[Direction.values().length];
        for (final Direction dir : Direction.values()) {
            final BlockPos offPos = pos.relative(dir);
            final BlockState state = serverLevel.getBlockState(offPos);
            unsmothered[dir.ordinal()] = !Block.isShapeFullBlock(state.getShape(serverLevel, offPos).getFaceShape(dir.getOpposite()));
        }
        for (final ShapeFaces faces : shapeFaces) {
            faces.forEach((dir, face) -> {
                if (unsmothered[dir.ordinal()] || face.depth() < 1) {
                    final TileKey key = new TileKey(pos, dir, face.depth());
                    final GServerDataHolder data = manager.getOrCreate(key.pos(), key.dir(), key.depth());
                    data.fillFromTo(color, face.x1(), face.y1(), face.x2(), face.y2());
                    manager.markDirty(data, pos, dir);
                }
            });
        }
        chunk.markUnsaved();
    }

    public static void latherInPaint(final ServerLevel serverLevel,
                                     final List<ShapeFaces> shapeFaces,
                                     final BlockPos pos,
                                     final Direction thisDir,
                                     final int color,
                                     final Vector4i gradient
    ) {
        final ChunkAccess chunk = serverLevel.getChunk(pos);
        final GServerChunkManager manager = chunk.getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER);

        final BlockPos offPos = pos.relative(thisDir);
        final BlockState state = serverLevel.getBlockState(offPos);
        if (Block.isShapeFullBlock(state.getShape(serverLevel, offPos).getFaceShape(thisDir.getOpposite()))) {
            return;
        }

        for (final ShapeFaces faces : shapeFaces) {
            faces.forEach((dir, face) -> {
                if (dir == thisDir) {
                    final TileKey key = new TileKey(pos, dir, face.depth());
                    final GServerDataHolder data = manager.getOrCreate(key.pos(), key.dir(), key.depth());
                    data.partialFillFromTo(color, face.x1(), face.y1(), face.x2(), face.y2(), gradient, serverLevel.getRandom());
                    manager.markDirty(data, pos, dir);
                }
            });
        }
        chunk.markUnsaved();
    }

    public record ShapeFaces(Face up, Face down, Face north, Face east, Face south, Face west) {
        public void forEach(final BiConsumer<Direction, Face> consumer) {
            if (this.up != null) {
                consumer.accept(Direction.UP, this.up);
            }
            if (this.down != null) {
                consumer.accept(Direction.DOWN, this.down);
            }
            if (this.north != null) {
                consumer.accept(Direction.NORTH, this.north);
            }
            if (this.east != null) {
                consumer.accept(Direction.EAST, this.east);
            }
            if (this.south != null) {
                consumer.accept(Direction.SOUTH, this.south);
            }
            if (this.west != null) {
                consumer.accept(Direction.WEST, this.west);
            }
        }
    }

    public record Face(double depth, int x1, int y1, int x2, int y2) {}
}
