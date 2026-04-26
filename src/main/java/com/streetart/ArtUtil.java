package com.streetart;

import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.GraffitiKey;
import com.streetart.managers.GServerChunkManager;
import com.streetart.managers.data.GServerDataHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ArtUtil {

    public static ColorComponent generateComponentFromByte(final byte content) {
        return ColorComponent.BY_ID.apply(content);
    }

    public static Vector2i calculatePixelCoordinates(final BlockHitResult hitResult) {
        final BlockPos pos = hitResult.getBlockPos();
        final Vec3 clickPosition = hitResult.getLocation();
        final Vector3f relative = new Vector3f(
                (float) (clickPosition.x - pos.getX()),
                (float) (clickPosition.y - pos.getY()),
                (float) (clickPosition.z - pos.getZ())
        );
        final Vec2 plane = switch (hitResult.getDirection()) {
            case DOWN -> new Vec2(relative.x, 1 - relative.z);
            case UP -> new Vec2(relative.x, relative.z);
            case NORTH -> new Vec2(1 - relative.x, 1 - relative.y);
            case SOUTH -> new Vec2(relative.x, 1 - relative.y);
            case WEST -> new Vec2(relative.z, 1 - relative.y);
            case EAST -> new Vec2(1 - relative.z, 1 - relative.y);
        };
        final int x = (int) (plane.x * 16);
        final int y = (int) (plane.y * 16);
        return new Vector2i(x, y);
    }

    public static int calculateDepth(final BlockHitResult hitResult) {
        final Vec3 relativePos = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(hitResult.getBlockPos()));
        double depth = switch (hitResult.getDirection().getAxis()) {
            case X -> relativePos.x;
            case Y -> relativePos.y;
            case Z -> relativePos.z;
        };

        if (hitResult.getDirection().getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            depth = 1 - depth;
        }
        return Mth.clamp(Mth.floor(depth * 16), 0, 15);
    }

    /**
     * Coats every exposed face of a block in paint
     *
     * @return true if any paint was applied
     */
    public static boolean latherInPaint(@Nullable final Entity entity,
                                        final ServerLevel serverLevel,
                                        final List<ShapeFaces> shapeFaces,
                                        final BlockPos pos,
                                        final byte content
    ) {
        final ChunkAccess chunk = serverLevel.getChunk(pos);
        final GServerChunkManager manager = chunk.getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER);

        boolean changed = false;

        final Identifier defaultLayer = AllGraffitiLayers.DEFAULT_LAYER.graffityLayerId();
        for (final ShapeFaces faces : shapeFaces) {
            changed |= faces.forEach((dir, face) -> {
                final BlockPos offPos = pos.relative(dir);
                final BlockState state = serverLevel.getBlockState(offPos);
                if (Block.isShapeFullBlock(state.getCollisionShape(serverLevel, offPos).getFaceShape(dir.getOpposite()))) {
                    return false;
                }

                final GraffitiKey key = new GraffitiKey(defaultLayer, pos, dir, face.depth());
                final GServerDataHolder data = manager.getOrConditionalCreateFace(defaultLayer, key.pos(), key.dir(), key.depth(), content == ColorComponent.CLEAR.id);
                if (data != null) {
                    data.fillFromTo(content, face.x1(), face.y1(), face.x2(), face.y2());
                    manager.markFullResend(defaultLayer, data, pos, dir);
                    manager.blame(entity, key.pos());
                    return true;
                }

                return false;
            });
        }

        if (changed) {
            chunk.markUnsaved();
        }
        return changed;
    }

    public static void latherDirectionInPaint(@Nullable final Entity entity,
                                              final ServerLevel serverLevel,
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
        if (Block.isShapeFullBlock(state.getCollisionShape(serverLevel, offPos).getFaceShape(thisDir.getOpposite()))) {
            return;
        }

        final Identifier defaultLayer = AllGraffitiLayers.DEFAULT_LAYER.graffityLayerId();

        boolean changed = false;
        for (final ShapeFaces faces : shapeFaces) {
            changed |= faces.doWith(thisDir, face -> {
                final GraffitiKey key = new GraffitiKey(defaultLayer, pos, thisDir, face.depth());
                final GServerDataHolder data = manager.getOrConditionalCreateFace(defaultLayer, key.pos(), key.dir(), key.depth(), content == ColorComponent.CLEAR.id);
                if (data != null) {
                    data.partialFillFromTo(content, face.x1(), face.y1(), face.x2(), face.y2(), gradient, serverLevel.getRandom());
                    manager.markFullResend(defaultLayer, data, pos, thisDir);
                    manager.blame(entity, key.pos());
                    return true;
                }
                return false;
            });
        }

        if (changed) {
            chunk.markUnsaved();
        }
    }

    public static List<ShapeFaces> gatherShapeFaces(final VoxelShape shape) {
        final List<ShapeFaces> faces = new ArrayList<>();
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            final int ix1 = Mth.clamp(Mth.floor(x1 * 16), 0, 16);
            final int iy1 = Mth.clamp(Mth.floor(y1 * 16), 0, 16);
            final int iz1 = Mth.clamp(Mth.floor(z1 * 16), 0, 16);
            final int ix2 = Mth.clamp(Mth.ceil(x2 * 16), 0, 16);
            final int iy2 = Mth.clamp(Mth.ceil(y2 * 16), 0, 16);
            final int iz2 = Mth.clamp(Mth.ceil(z2 * 16), 0, 16);
            faces.add(new ShapeFaces(
                    new Face(Mth.clamp(16 - iy2, 0, 15),
                            ix1, iz1, ix2, iz2),
                    new Face(Mth.clamp(iy1, 0, 15),
                            ix1, 16 - iz2, ix2, 16 - iz1),
                    new Face(Mth.clamp(iz1, 0, 15),
                            16 - ix2, 16 - iy2, 16 - ix1, 16 - iy1),
                    new Face(Mth.clamp(16 - ix2, 0, 15),
                            16 - iz2, 16 - iy2, 16 - iz1, 16 - iy1),
                    new Face(Mth.clamp(16 - iz2, 0, 15),
                            ix1, 16 - iy2, ix2, 16 - iy1),
                    new Face(Mth.clamp(ix1, 0, 15),
                            iz1, 16 - iy2, iz2, 16 - iy1)
            ));
        });
        return faces;
    }

    public record ShapeFaces(Face up, Face down, Face north, Face east, Face south, Face west) {
        public <T> T doWith(final Direction dir, final Function<Face, T> function) { // mildly horrifying sentence
            return switch (dir) {
                case DOWN -> function.apply(this.down);
                case UP -> function.apply(this.up);
                case NORTH -> function.apply(this.north);
                case SOUTH -> function.apply(this.south);
                case WEST -> function.apply(this.west);
                case EAST -> function.apply(this.east);
            };
        }

        /**
         * @return true if any function call returns true
         */
        public boolean forEach(final BiFunction<Direction, Face, Boolean> function) {
            boolean value = false;
            if (this.up != null) {
                value |= function.apply(Direction.UP, this.up);
            }
            if (this.down != null) {
                value |= function.apply(Direction.DOWN, this.down);
            }
            if (this.north != null) {
                value |= function.apply(Direction.NORTH, this.north);
            }
            if (this.east != null) {
                value |= function.apply(Direction.EAST, this.east);
            }
            if (this.south != null) {
                value |= function.apply(Direction.SOUTH, this.south);
            }
            if (this.west != null) {
                value |= function.apply(Direction.WEST, this.west);
            }
            return value;
        }
    }

    public record Face(int depth, int x1, int y1, int x2, int y2) {}
}
