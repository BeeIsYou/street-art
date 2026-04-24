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

import java.util.Collection;
import java.util.List;

public class TrackRenderer {
    public static void render(final LevelTerrainRenderContext context) {
        final List<RecordedTrack.Point> currentRecording = StreetArt.recordingManager.getPoints();
        final Collection<RecordedTrack> heldRecordings = StreetArt.recordingManager.findInventoryRecordings(Minecraft.getInstance().player);

        final SubmitNodeStorage storage = ((LevelRendererAccessor)context.levelRenderer()).getSubmitNodeStorage();

        final PoseStack poseStack = new PoseStack();
        final Vec3 camPos = context.levelState().cameraRenderState.pos;

        final RenderType renderType = RenderTypes.lines();
        final double partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final double offset = 1 - (partialTick + context.levelState().gameTime % 20) / 20f;
        storage.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            if (currentRecording != null) {
                renderTrack(pose, buffer, camPos, currentRecording,
                        DyeColor.WHITE.getTextColor(),
                        DyeColor.GRAY.getTextColor(),
                        offset, 0.05f
                );
            }
            for (final RecordedTrack recording : heldRecordings) {
                renderTrack(pose, buffer, camPos, recording.getPoints(),
                        recording.colorA.getTextColor(),
                        recording.colorB.getTextColor(),
                        offset, 0.05f
                );
            }
        });
    }

    private static void renderTrack(final PoseStack.Pose pose, final VertexConsumer buffer, final Vec3 camPos, final List<RecordedTrack.Point> points,
                                    final int startColor, final int endColor, final double progressOffset, final float frequency) {
        final float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
        float progress = (float)(progressOffset % 1);
        int color = mixColors(progress, startColor, endColor);
        for (int i = 1; i < points.size(); i++) {
            final float nextProgress = (progress + frequency) % 1;
            final int nextColor = mixColors(nextProgress, startColor, endColor);
            renderLine(pose, buffer, camPos,
                    points.get(i - 1), points.get(i),
                    color, nextColor, width);
            progress = nextProgress;
            color = nextColor;
        }
    }

    private static void renderLine(final PoseStack.Pose pose, final VertexConsumer buffer, final Vec3 camPos,
                                   final RecordedTrack.Point a, final RecordedTrack.Point b,
                                   final int colorA, final int colorB, final float baseWidth) {
        buffer.addVertex(
                        (float) (a.x() - camPos.x),
                        (float) (a.y() - camPos.y),
                        (float) (a.z() - camPos.z))
                .setColor(colorA)
                .setLineWidth(baseWidth)
                .setNormal((float) (b.x() - a.x()), (float) (b.y() - a.y()), (float) (b.z() - a.z()));
        buffer.addVertex(
                        (float) (b.x() - camPos.x),
                        (float) (b.y() - camPos.y),
                        (float) (b.z() - camPos.z))
                .setColor(colorB)
                .setLineWidth(baseWidth)
                .setNormal((float) (b.x() - a.x()), (float) (b.y() - a.y()), (float) (b.z() - a.z()));
    }

    public static int mixColors(final float mix, final int from, final int to) {
        final float invMix = 1 - mix;
        final int r = (int) ((from & 0xFF) * invMix + (to & 0xFF) * mix);
        final int g = (int) (((from >> 8) & 0xFF) * invMix + ((to >> 8) & 0xFF) * mix);
        final int b = (int) (((from >> 16) & 0xFF) * invMix + ((to >> 16) & 0xFF) * mix);
        return r | g << 8 | b << 16 | 0xFF000000;
    }
}
