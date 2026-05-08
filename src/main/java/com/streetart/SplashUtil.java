package com.streetart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SplashUtil {
    /**
     * Performs raycasts outwards from the origin to add many decals in a roughly realistic manner
     *
     * @param range          The distance in blocks for raycasts to travel
     * @param intensityScale scalar for how many "hits" are required for full coverage. bigger = more. 1 -> 100 hits over four adjacent faces for max
     */
    public static void createPaintSplash(@Nullable final Entity entity,
                                         final ServerLevel level,
                                         final Vec3 origin,
                                         final double range,
                                         final int rays,
                                         final float intensityScale,
                                         final VariableThreshold threshold,
                                         final byte content,
                                         final Predicate<BlockPos> modificationAllowed) {
        final SplashExposure exposure = collectBlocks(level, origin, rays, range);
        // todo layer
        applyPaint(AllGraffitiLayers.DEFAULT_LAYER.identifier(), entity, level, exposure, intensityScale, threshold, content, modificationAllowed);
    }

    /**
     * Casts rays in a sphere and counts how many hit each direction of each block
     */
    public static SplashExposure collectBlocks(final Level level, final Vec3 origin, final int rays, final double range) {
        final SplashExposure exposure = new SplashExposure();
        for (int i = 0; i < rays; i++) {
            // fibonacci sphere
            // https://extremelearning.com.au/how-to-evenly-distribute-points-on-a-sphere-more-effectively-than-the-canonical-fibonacci-lattice/
            // ignore the fact that the url talks about how to do better than this
            final double theta = Math.TAU * i / 1.61803398875d; // golden ratio
            final double phi = Math.acos(1 - 2d * i / rays);
            final Vec3 dirVec = new Vec3(
                    Math.cos(theta) * Math.sin(phi),
                    Math.cos(phi),
                    Math.sin(theta) * Math.sin(phi)
            );

            final BlockHitResult result = level.clip(new ClipContext(
                    origin,
                    origin.add(dirVec.scale(range)),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    CollisionContext.empty())
            );
            if (result.getType() == HitResult.Type.BLOCK) {
                exposure.increment(result.getBlockPos(), result.getDirection());
            }
        }

        return exposure;
    }

    /**
     * Gathers the 3x3 grid of adjacent faces and does Math on them
     *
     * @param intensityScale the "intensity" of the expose. 1 means that 100 hits on the four surrounding faces is full exposure
     * @return the sum of the hits in a 2x2 square centered on each corner of the central block face
     */
    private static Vector4f getHitsGradient(final SplashExposure exposure,
                                            final BlockPos pos, final Direction dir,
                                            float intensityScale) {
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
                parts[i + j * 3 + 4] = exposure.get(offPos, dir);
            }
        }
        return new Vector4f(
                Math.clamp((parts[0] + parts[1] + parts[3] + parts[4]) * intensityScale, 0, 1),
                Math.clamp((parts[1] + parts[2] + parts[4] + parts[5]) * intensityScale, 0, 1),
                Math.clamp((parts[3] + parts[4] + parts[6] + parts[7]) * intensityScale, 0, 1),
                Math.clamp((parts[4] + parts[5] + parts[7] + parts[8]) * intensityScale, 0, 1)
        );
    }

    public static void applyPaint(final Identifier layer,
                                  @Nullable final Entity entity,
                                  final ServerLevel level,
                                  final SplashExposure exposure,
                                  final float intensityScale,
                                  final VariableThreshold threshold,
                                  final byte content,
                                  final Predicate<BlockPos> modificationAllowed) {
        for (final Map.Entry<BlockPos, EnumMap<Direction, Integer>> blockEntry : exposure.entrySet()) {
            final BlockPos pos = blockEntry.getKey();
            if (modificationAllowed.test(pos)) {
                final List<ArtUtil.ShapeFaces> faces = ArtUtil.gatherShapeFaces(level.getBlockState(pos).getCollisionShape(level, pos));
                blockEntry.getValue().forEach((dir, hits) -> {
                    final Vector4f hitsGradient = getHitsGradient(exposure, pos, dir, 0.01f * intensityScale);
                    ArtUtil.latherDirectionInPaint(layer, entity, level, faces, pos, dir, content, hitsGradient, threshold);
                });
            }
        }
    }

    public static class SplashExposure extends HashMap<BlockPos, EnumMap<Direction, Integer>> {
        public void increment(final BlockPos pos, final Direction dir) {
            this.computeIfAbsent(pos, _ -> new EnumMap<>(Direction.class))
                    .compute(dir, (_, c) -> c == null ? 1 : c + 1);
        }

        public int get(final BlockPos pos, final Direction dir) {
            final EnumMap<Direction, Integer> map = this.get(pos);
            if (map == null) {
                return 0;
            }
            return map.getOrDefault(dir, 0);
        }
    }

    @FunctionalInterface
    public interface VariableThreshold {
        float get(double x, double y, double z, Direction dir);

        VariableThreshold FLAT = (_, _, _, _) -> 0.5f;

        static VariableThreshold random(final RandomSource randomSource) {
            return (_, _, _, _) -> randomSource.nextFloat();
        }

        static VariableThreshold perlin(final RandomSource randomSource) {
            final PerlinNoise noise = PerlinNoise.create(randomSource, 1, 1);
            return (x, y, z, _) -> (float)noise.getValue(x, y, z) * 0.5f + 0.5f;
        }

        static VariableThreshold test() {
            return (x, y, z, _) -> {
                x = ((x % 1) + 1) % 1;
                y = ((y % 1) + 1) % 1;
                z = ((z % 1) + 1) % 1;
                return (float) ((x + y + z) / 3);
            };
        }
    }
}
