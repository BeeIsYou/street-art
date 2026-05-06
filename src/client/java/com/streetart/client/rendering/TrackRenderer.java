package com.streetart.client.rendering;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.streetart.StreetArt;
import com.streetart.item.TapeRecorderItem;
import com.streetart.tracks.RecordedTrack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TrackRenderer {
    public static final RenderPipeline BOX_ON_TOP = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(StreetArt.id("box"))
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .build()
    );
    public static final RenderType BOX_RENDERTYPE = RenderType.create("street_art:box",
            RenderSetup.builder(BOX_ON_TOP)
                    .sortOnUpload()
                    .affectsCrumbling()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
    );

    public static void render(final SubmitNodeStorage storage, final Vec3 camPos, final long gameTime) {
        final List<RecordedTrack.Point> currentRecording = StreetArt.recordingManager.getPoints();
        final Collection<RecordedTrack> heldRecordings = StreetArt.recordingManager.findInventoryRecordings(Minecraft.getInstance().player);

        final PoseStack poseStack = new PoseStack();

        final double partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final double offset = 1 - (partialTick + gameTime % 20) / 20f;

        final RenderType lineType = RenderTypes.lines();
        storage.submitCustomGeometry(poseStack, lineType, (pose, buffer) -> {
            final List<RecordedTrack.Point> points = new ArrayList<>();
            points.add(new RecordedTrack.Point(0, 0, 0, false));
            points.add(new RecordedTrack.Point(0, 1, 0, false));
            renderTrack(pose, buffer, camPos,
                    points,
                    0xFF000000, -1,
                    0.5, 1f
            );
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

        final RecordedTrack heldTrack = TapeRecorderItem.findTrackInHand(Minecraft.getInstance().player);

        storage.order(1).submitCustomGeometry(poseStack, BOX_RENDERTYPE, (pose, buffer) -> {
            for (final RecordedTrack recording : heldRecordings) {
                if (recording.running) {
                    recording.partialTick = partialTick;
                }
                if (recording.running || recording == heldTrack) {
                    final double time = recording.progress + recording.partialTick;
                    renderPoint(pose, buffer, camPos, recording.colorB.getTextColor(), recording.getPoints(), time);
                }
            }
        });
    }

    private static void renderPoint(final PoseStack.Pose pose, final VertexConsumer buffer, final Vec3 camPos,
                                    final int color, final List<RecordedTrack.Point> points, final double time) {
        final Vector3f pos;
        if (points.size() >= 2) {
            int i = Mth.floor(time);
            i = Math.clamp(i, 0, points.size() - 2);
            final double frac = Math.clamp(time - i, 0, 1);
            final RecordedTrack.Point pointA = points.get(i);
            final RecordedTrack.Point pointB = points.get(i + 1);
            pos = new Vector3f(
                    (float) (pointA.x() * (1 - frac) + pointB.x() * frac - camPos.x),
                    (float) (pointA.y() * (1 - frac) + pointB.y() * frac - camPos.y),
                    (float) (pointA.z() * (1 - frac) + pointB.z() * frac - camPos.z)
            );
        } else if (points.isEmpty()) {
            return;
        } else {
            pos = new Vector3f(
                    (float) (points.getFirst().x() - camPos.x),
                    (float) (points.getFirst().x() - camPos.y),
                    (float) (points.getFirst().x() - camPos.z)
            );
        }
        pos.y += 0.2f;
        final float s = 0.25f;

        // -x face
        buffer.addVertex(pos.x - s, pos.y - s, pos.z - s).setColor(color);
        buffer.addVertex(pos.x - s, pos.y - s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x - s, pos.y + s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x - s, pos.y + s, pos.z - s).setColor(color);
        // +x face
        buffer.addVertex(pos.x + s, pos.y + s, pos.z - s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y + s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y - s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y - s, pos.z - s).setColor(color);
        // -y face
        buffer.addVertex(pos.x - s, pos.y - s, pos.z - s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y - s, pos.z - s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y - s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x - s, pos.y - s, pos.z + s).setColor(color);
        // +y face
        buffer.addVertex(pos.x - s, pos.y + s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y + s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y + s, pos.z - s).setColor(color);
        buffer.addVertex(pos.x - s, pos.y + s, pos.z - s).setColor(color);
        // -z face
        buffer.addVertex(pos.x - s, pos.y - s, pos.z - s).setColor(color);
        buffer.addVertex(pos.x - s, pos.y + s, pos.z - s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y + s, pos.z - s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y - s, pos.z - s).setColor(color);
        // +z face
        buffer.addVertex(pos.x + s, pos.y - s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x + s, pos.y + s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x - s, pos.y + s, pos.z + s).setColor(color);
        buffer.addVertex(pos.x - s, pos.y - s, pos.z + s).setColor(color);
    }

    private static void renderTrack(final PoseStack.Pose pose, final VertexConsumer buffer, final Vec3 camPos, final List<RecordedTrack.Point> points,
                                    final int startColor, final int endColor, final double progressOffset, final float frequency) {
        final float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
        double progress = (progressOffset % 1);
        int color = mixColors(progress, startColor, endColor);
        for (int i = 1; i < points.size(); i++) {
            final RecordedTrack.Point a = points.get(i - 1);
            final RecordedTrack.Point b = points.get(i);
            double nextProgress = progress + frequency;
            final int nextColor;
            if (nextProgress > 1) {
                final double mid = (1 - progress) / frequency;
                nextProgress %= 1;
                nextColor = mixColors(nextProgress, startColor, endColor);
                final Vector3d middle = new Vector3d(
                    a.x() * (1 - mid) + b.x() * mid,
                    a.y() * (1 - mid) + b.y() * mid,
                    a.z() * (1 - mid) + b.z() * mid
                );
                renderLine(pose, buffer, camPos,
                        a.x(), a.y(), a.z(),
                        middle.x, middle.y, middle.z,
                        endColor, color, width
                );
                renderLine(pose, buffer, camPos,
                        middle.x, middle.y, middle.z,
                        b.x(), b.y(), b.z(),
                        nextColor, startColor, width
                );
            } else {
                nextColor = mixColors(nextProgress, startColor, endColor);
                renderLine(pose, buffer, camPos,
                        a.x(), a.y(), a.z(),
                        b.x(), b.y(), b.z(),
                        color, nextColor, width);
            }
            progress = nextProgress;
            color = nextColor;
        }
    }

    private static void renderLine(final PoseStack.Pose pose, final VertexConsumer buffer, final Vec3 camPos,
                                   final double xa, final double ya, final double za,
                                   final double xb, final double yb, final double zb,
                                   final int colorA, final int colorB, final float baseWidth) {
        final float length = (float) Math.sqrt((xa-xb)*(xa-xb)+(ya-yb)*(ya-yb)+(za-zb)*(za-zb));
        final float nx = (float) ((xa-xb)/length);
        final float ny = (float) ((ya-yb)/length);
        final float nz = (float) ((za-zb)/length);
        buffer.addVertex(
                        (float) (xa - camPos.x),
                        (float) (ya - camPos.y),
                        (float) (za - camPos.z))
                .setColor(colorA)
                .setLineWidth(baseWidth)
                .setNormal(nx, ny, nz);
        buffer.addVertex(
                        (float) (xb - camPos.x),
                        (float) (yb - camPos.y),
                        (float) (zb - camPos.z))
                .setColor(colorB)
                .setLineWidth(baseWidth)
                .setNormal(nx, ny, nz);
    }

    public static int mixColors(final double mix, final int from, final int to) {
        final double invMix = 1 - mix;
        final int r = (int) ((from & 0xFF) * invMix + (to & 0xFF) * mix);
        final int g = (int) (((from >> 8) & 0xFF) * invMix + ((to >> 8) & 0xFF) * mix);
        final int b = (int) (((from >> 16) & 0xFF) * invMix + ((to >> 16) & 0xFF) * mix);
        return r | g << 8 | b << 16 | 0xFF000000;
    }
}
