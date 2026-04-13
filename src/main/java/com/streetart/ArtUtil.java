package com.streetart;

import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.TileKey;
import com.streetart.managers.GServerChunkManager;
import com.streetart.managers.GServerDataHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ArtUtil {

    public static ColorComponent generateComponentFromByte(final byte content) {
        return ColorComponent.BY_ID.apply(content);
    }

    public static byte generateByteFromColor(@NotNull final ColorComponent color) {
        return color.id;
    }

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

    public static void latherInPaint(final ServerLevel serverLevel,
                                     final List<ShapeFaces> shapeFaces,
                                     final BlockPos pos,
                                     final Direction thisDir,
                                     final byte content,
                                     final Vector4f gradient
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
                    data.partialFillFromTo(content, face.x1(), face.y1(), face.x2(), face.y2(), gradient, serverLevel.getRandom());
                    manager.markDirty(data, pos, dir);
                }
            });
        }
        chunk.markUnsaved();
    }

    public static List<ShapeFaces> gatherShapeFaces(final VoxelShape shape) {
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
