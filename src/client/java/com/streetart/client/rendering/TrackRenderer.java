package com.streetart.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.streetart.StreetArt;
import com.streetart.client.mixin.LevelRendererAccessor;
import com.streetart.tracks.TrackRecording;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class TrackRenderer {
    public static void render(final LevelTerrainRenderContext context) {
        final TrackRecording recording = StreetArt.recordingManager.findHighestPriorityRecording(Minecraft.getInstance().player);
        if (recording == null) {
            return;
        }

        final SubmitNodeStorage storage = ((LevelRendererAccessor)context.levelRenderer()).getSubmitNodeStorage();

        final PoseStack poseStack = new PoseStack();
        final Vec3 camPos = context.levelState().cameraRenderState.pos;

        final RenderType renderType = RenderTypes.debugPoint();
        storage.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> renderTrack(pose, buffer, camPos, recording));
    }

    private static void renderTrack(final PoseStack.Pose pose, final VertexConsumer buffer, final Vec3 camPos, final TrackRecording recording) {
        for (final TrackRecording.Point point : recording.getPoints()) {
            buffer.addVertex(
                    (float)(point.x() - camPos.x),
                    (float)(point.y() - camPos.y),
                    (float)(point.z() - camPos.z))
                    .setColor(point.significant() ? Color.YELLOW.getRGB() : Color.WHITE.getRGB())
                    .setLineWidth(10);
        }
    }
}
