package com.streetart.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.streetart.StreetArt;
import com.streetart.client.mixin.LevelRendererAccessor;
import com.streetart.tracks.RecordedTrack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TrackRenderer {
    public static void render(final LevelTerrainRenderContext context) {
        final List<RecordedTrack.Point> currentRecording = StreetArt.recordingManager.getPoints();
        final RecordedTrack heldRecording = StreetArt.recordingManager.findInventoryRecording(Minecraft.getInstance().player);

        final SubmitNodeStorage storage = ((LevelRendererAccessor)context.levelRenderer()).getSubmitNodeStorage();

        final PoseStack poseStack = new PoseStack();
        final Vec3 camPos = context.levelState().cameraRenderState.pos;

        final RenderType renderType = RenderTypes.lines();
        final double partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final double offset = (partialTick + context.levelState().gameTime % 20) * 0.05;
        storage.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            if (currentRecording != null) {
                renderTrack(pose, buffer, camPos, currentRecording,
                        DyeColor.WHITE.getTextureDiffuseColor(),
                        DyeColor.GRAY.getTextureDiffuseColor(),
                        offset, 0.05
                );
            }
            if (heldRecording != null) {
                renderTrack(pose, buffer, camPos, heldRecording.getPoints(),
                        heldRecording.colorA.getTextureDiffuseColor(),
                        heldRecording.colorB.getTextureDiffuseColor(),
                        offset, 0.05
                );
            }
        });
    }

    private static void renderTrack(final PoseStack.Pose pose, final VertexConsumer buffer, final Vec3 camPos, final List<RecordedTrack.Point> points,
                                    final int startColor, final int endColor, final double progressOffset, final double speed) {
        final float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth * 2;
        for (int i = 0; i < points.size() - 1; i++) {
            for (int j = 0; j < 2; j++) {
                final float progress = (float)(((progressOffset - (i + j) * speed) % 1) + 1) % 1;
                final int color = mixColors(progress, startColor, endColor);
                final RecordedTrack.Point point = points.get(i+j);
                buffer.addVertex(
                                (float) (point.x() - camPos.x),
                                (float) (point.y() - camPos.y),
                                (float) (point.z() - camPos.z))
                        .setColor(color)
                        .setLineWidth(width)
                        .setNormal(0, 1, 0);
            }
        }
    }

    public static int mixColors(final float mix, final int from, final int to) {
        final float invMix = 1 - mix;
        final int r = (int) ((from & 0xFF) * invMix + (to & 0xFF) * mix);
        final int g = (int) (((from >> 8) & 0xFF) * invMix + ((to >> 8) & 0xFF) * mix);
        final int b = (int) (((from >> 16) & 0xFF) * invMix + ((to >> 16) & 0xFF) * mix);
        return r | g << 8 | b << 16 | 0xFF000000;
    }
}
