package com.streetart.client.rendering;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.streetart.client.manager.GClientData;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Util;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;

public class LightMath {
    private static final ThreadLocal<BlockModelLighter.Cache> CACHE = ThreadLocal.withInitial(BlockModelLighter.Cache::new);
    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private static final BlockModelLighter.Cache cache = CACHE.get();

    /**
     * {@link BlockModelLighter#prepareQuadAmbientOcclusion(BlockAndTintGetter, BlockState, BlockPos, BakedQuad, QuadInstance)} please help me
     */
    public static void OhGodSoMuchMath(final GClientData data, final BlockAndTintGetter level, final BlockState state) {
        final Direction direction = data.dir;
        final boolean faceCubic = data.getDepth() == 1;
        final BlockPos centerPosition = data.pos;
        final BlockPos basePosition = faceCubic ? centerPosition.relative(direction) : centerPosition;
        final BlockModelLighter.AdjacencyInfo info = BlockModelLighter.AdjacencyInfo.fromFacing(direction);

        pos.setWithOffset(basePosition, info.corners[0]);
        final BlockState state0 = level.getBlockState(pos);
        final int light0 = cache.getLightCoords(state0, level, pos);
        final float shade0 = cache.getShadeBrightness(state0, level, pos);

        pos.setWithOffset(basePosition, info.corners[1]);
        final BlockState state1 = level.getBlockState(pos);
        final int light1 = cache.getLightCoords(state1, level, pos);
        final float shade1 = cache.getShadeBrightness(state1, level, pos);

        pos.setWithOffset(basePosition, info.corners[2]);
        final BlockState state2 = level.getBlockState(pos);
        final int light2 = cache.getLightCoords(state2, level, pos);
        final float shade2 = cache.getShadeBrightness(state2, level, pos);

        pos.setWithOffset(basePosition, info.corners[3]);
        final BlockState state3 = level.getBlockState(pos);
        final int light3 = cache.getLightCoords(state3, level, pos);
        final float shade3 = cache.getShadeBrightness(state3, level, pos);

        final BlockState corner0 = level.getBlockState(pos.setWithOffset(basePosition, info.corners[0]).move(direction));
        final boolean translucent0 = !corner0.isViewBlocking(level, pos) || corner0.getLightDampening() == 0;
        final BlockState corner1 = level.getBlockState(pos.setWithOffset(basePosition, info.corners[1]).move(direction));
        final boolean translucent1 = !corner1.isViewBlocking(level, pos) || corner1.getLightDampening() == 0;
        final BlockState corner2 = level.getBlockState(pos.setWithOffset(basePosition, info.corners[2]).move(direction));
        final boolean translucent2 = !corner2.isViewBlocking(level, pos) || corner2.getLightDampening() == 0;
        final BlockState corner3 = level.getBlockState(pos.setWithOffset(basePosition, info.corners[3]).move(direction));
        final boolean translucent3 = !corner3.isViewBlocking(level, pos) || corner3.getLightDampening() == 0;

        final float shadeCorner02;
        final int lightCorner02;
        if (!translucent2 && !translucent0) {
            shadeCorner02 = shade0;
            lightCorner02 = light0;
        } else {
            pos.setWithOffset(basePosition, info.corners[0]).move(info.corners[2]);
            final BlockState state02 = level.getBlockState(pos);
            shadeCorner02 = cache.getShadeBrightness(state02, level, pos);
            lightCorner02 = cache.getLightCoords(state02, level, pos);
        }

        final float shadeCorner03;
        final int lightCorner03;
        if (!translucent3 && !translucent0) {
            shadeCorner03 = shade0;
            lightCorner03 = light0;
        } else {
            pos.setWithOffset(basePosition, info.corners[0]).move(info.corners[3]);
            final BlockState state03 = level.getBlockState(pos);
            shadeCorner03 = cache.getShadeBrightness(state03, level, pos);
            lightCorner03 = cache.getLightCoords(state03, level, pos);
        }

        final float shadeCorner12;
        final int lightCorner12;
        if (!translucent2 && !translucent1) {
            shadeCorner12 = shade0;
            lightCorner12 = light0;
        } else {
            pos.setWithOffset(basePosition, info.corners[1]).move(info.corners[2]);
            final BlockState state12 = level.getBlockState(pos);
            shadeCorner12 = cache.getShadeBrightness(state12, level, pos);
            lightCorner12 = cache.getLightCoords(state12, level, pos);
        }

        final float shadeCorner13;
        final int lightCorner13;
        if (!translucent3 && !translucent1) {
            shadeCorner13 = shade0;
            lightCorner13 = light0;
        } else {
            pos.setWithOffset(basePosition, info.corners[1]).move(info.corners[3]);
            final BlockState state13 = level.getBlockState(pos);
            shadeCorner13 = cache.getShadeBrightness(state13, level, pos);
            lightCorner13 = cache.getLightCoords(state13, level, pos);
        }

        int lightCenter = cache.getLightCoords(state, level, centerPosition);
        pos.setWithOffset(centerPosition, direction);
        final BlockState nextState = level.getBlockState(pos);
        if (faceCubic || !nextState.isSolidRender()) {
            lightCenter = cache.getLightCoords(nextState, level, pos);
        }

        final float shadeCenter = faceCubic
                ? cache.getShadeBrightness(level.getBlockState(basePosition), level, basePosition)
                : cache.getShadeBrightness(level.getBlockState(centerPosition), level, centerPosition);
        final BlockModelLighter.AmbientVertexRemap remap = BlockModelLighter.AmbientVertexRemap.fromFacing(direction);

        final float tempShade1 = (shade3 + shade0 + shadeCorner03 + shadeCenter) * 0.25F;
        final float tempShade2 = (shade2 + shade0 + shadeCorner02 + shadeCenter) * 0.25F;
        final float tempShade3 = (shade2 + shade1 + shadeCorner12 + shadeCenter) * 0.25F;
        final float tempShade4 = (shade3 + shade1 + shadeCorner13 + shadeCenter) * 0.25F;
//        final boolean facePartial = data.getDepth() < 1;

        final int[] colors = new int[4];
        final int[] lights = new int[4];

        /*if (facePartial *//*&& info.doNonCubicWeight*//*) {
            final float vert0weight01 = AO_FACE_SHAPES[info.vert0Weights[0].index] * AO_FACE_SHAPES[info.vert0Weights[1].index];
            final float vert0weight23 = AO_FACE_SHAPES[info.vert0Weights[2].index] * AO_FACE_SHAPES[info.vert0Weights[3].index];
            final float vert0weight45 = AO_FACE_SHAPES[info.vert0Weights[4].index] * AO_FACE_SHAPES[info.vert0Weights[5].index];
            final float vert0weight67 = AO_FACE_SHAPES[info.vert0Weights[6].index] * AO_FACE_SHAPES[info.vert0Weights[7].index];
            final float vert1weight01 = AO_FACE_SHAPES[info.vert1Weights[0].index] * AO_FACE_SHAPES[info.vert1Weights[1].index];
            final float vert1weight23 = AO_FACE_SHAPES[info.vert1Weights[2].index] * AO_FACE_SHAPES[info.vert1Weights[3].index];
            final float vert1weight45 = AO_FACE_SHAPES[info.vert1Weights[4].index] * AO_FACE_SHAPES[info.vert1Weights[5].index];
            final float vert1weight67 = AO_FACE_SHAPES[info.vert1Weights[6].index] * AO_FACE_SHAPES[info.vert1Weights[7].index];
            final float vert2weight01 = AO_FACE_SHAPES[info.vert2Weights[0].index] * AO_FACE_SHAPES[info.vert2Weights[1].index];
            final float vert2weight23 = AO_FACE_SHAPES[info.vert2Weights[2].index] * AO_FACE_SHAPES[info.vert2Weights[3].index];
            final float vert2weight45 = AO_FACE_SHAPES[info.vert2Weights[4].index] * AO_FACE_SHAPES[info.vert2Weights[5].index];
            final float vert2weight67 = AO_FACE_SHAPES[info.vert2Weights[6].index] * AO_FACE_SHAPES[info.vert2Weights[7].index];
            final float vert3weight01 = AO_FACE_SHAPES[info.vert3Weights[0].index] * AO_FACE_SHAPES[info.vert3Weights[1].index];
            final float vert3weight23 = AO_FACE_SHAPES[info.vert3Weights[2].index] * AO_FACE_SHAPES[info.vert3Weights[3].index];
            final float vert3weight45 = AO_FACE_SHAPES[info.vert3Weights[4].index] * AO_FACE_SHAPES[info.vert3Weights[5].index];
            final float vert3weight67 = AO_FACE_SHAPES[info.vert3Weights[6].index] * AO_FACE_SHAPES[info.vert3Weights[7].index];
            colors[0] = ARGB.gray(Math.clamp(tempShade1 * vert0weight01 + tempShade2 * vert0weight23 + tempShade3 * vert0weight45 + tempShade4 * vert0weight67, 0.0F, 1.0F));
            colors[1] = ARGB.gray(Math.clamp(tempShade1 * vert1weight01 + tempShade2 * vert1weight23 + tempShade3 * vert1weight45 + tempShade4 * vert1weight67, 0.0F, 1.0F));
            colors[2] = ARGB.gray(Math.clamp(tempShade1 * vert2weight01 + tempShade2 * vert2weight23 + tempShade3 * vert2weight45 + tempShade4 * vert2weight67, 0.0F, 1.0F));
            colors[3] = ARGB.gray(Math.clamp(tempShade1 * vert3weight01 + tempShade2 * vert3weight23 + tempShade3 * vert3weight45 + tempShade4 * vert3weight67, 0.0F, 1.0F));
            final int _tc1 = LightCoordsUtil.smoothBlend(light3, light0, lightCorner03, lightCenter);
            final int _tc2 = LightCoordsUtil.smoothBlend(light2, light0, lightCorner02, lightCenter);
            final int _tc3 = LightCoordsUtil.smoothBlend(light2, light1, lightCorner12, lightCenter);
            final int _tc4 = LightCoordsUtil.smoothBlend(light3, light1, lightCorner13, lightCenter);
            lights[0] = LightCoordsUtil.smoothWeightedBlend(_tc1, _tc2, _tc3, _tc4, vert0weight01, vert0weight23, vert0weight45, vert0weight67);
            lights[1] = LightCoordsUtil.smoothWeightedBlend(_tc1, _tc2, _tc3, _tc4, vert1weight01, vert1weight23, vert1weight45, vert1weight67);
            lights[2] = LightCoordsUtil.smoothWeightedBlend(_tc1, _tc2, _tc3, _tc4, vert2weight01, vert2weight23, vert2weight45, vert2weight67);
            lights[3] = LightCoordsUtil.smoothWeightedBlend(_tc1, _tc2, _tc3, _tc4, vert3weight01, vert3weight23, vert3weight45, vert3weight67);
        } else {*/
            colors[0] = ARGB.gray(tempShade1);
            colors[1] = ARGB.gray(tempShade2);
            colors[2] = ARGB.gray(tempShade3);
            colors[3] = ARGB.gray(tempShade4);
            lights[0] = LightCoordsUtil.smoothBlend(light3, light0, lightCorner03, lightCenter);
            lights[1] = LightCoordsUtil.smoothBlend(light2, light0, lightCorner02, lightCenter);
            lights[2] = LightCoordsUtil.smoothBlend(light2, light1, lightCorner12, lightCenter);
            lights[3] = LightCoordsUtil.smoothBlend(light3, light1, lightCorner13, lightCenter);
//        }
        final CardinalLighting cardinalLighting = level.cardinalLighting();
        final float scale = cardinalLighting.byFace(direction);
        colors[0] = ARGB.scaleRGB(colors[0], scale);
        colors[1] = ARGB.scaleRGB(colors[1], scale);
        colors[2] = ARGB.scaleRGB(colors[2], scale);
        colors[3] = ARGB.scaleRGB(colors[3], scale);

        VertexRemap.apply(colors, lights, data, direction);
    }

    public enum VertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(1, 2, 3, 0),
        SOUTH(0, 1, 2, 3),
        WEST(1, 2, 3, 0),
        EAST(3, 0, 1, 2);

        private final int vert0;
        private final int vert1;
        private final int vert2;
        private final int vert3;
        private static final VertexRemap[] BY_FACING = Util.make(new VertexRemap[6], (map) -> {
            map[Direction.DOWN.get3DDataValue()] = DOWN;
            map[Direction.UP.get3DDataValue()] = UP;
            map[Direction.NORTH.get3DDataValue()] = NORTH;
            map[Direction.SOUTH.get3DDataValue()] = SOUTH;
            map[Direction.WEST.get3DDataValue()] = WEST;
            map[Direction.EAST.get3DDataValue()] = EAST;
        });

        VertexRemap(final int vert0, final int vert1, final int vert2, final int vert3) {
            this.vert0 = vert0;
            this.vert1 = vert1;
            this.vert2 = vert2;
            this.vert3 = vert3;
        }

        public void apply(final int[] colors, final int[] lights, final GClientData data) {
            data.light0 = lights[this.vert0];
            data.color0 = colors[this.vert0];

            data.light1 = lights[this.vert1];
            data.color1 = colors[this.vert1];

            data.light2 = lights[this.vert2];
            data.color2 = colors[this.vert2];

            data.light3 = lights[this.vert3];
            data.color3 = colors[this.vert3];
        }

        public static void apply(final int[] colors, final int[] lights, final GClientData data, final Direction dir) {
            fromFacing(dir).apply(colors, lights, data);
        }

        public static VertexRemap fromFacing(final Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }
    }
}
