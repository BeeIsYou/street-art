package com.streetart.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.streetart.client.mixin.LevelRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

public class GraffitiRenderer {
    public static void render(LevelTerrainRenderContext context) {
        SubmitNodeStorage storage = ((LevelRendererAccessor)context.levelRenderer()).getSubmitNodeStorage();

        PoseStack pose = new PoseStack();
        Vec3 camPos = context.levelState().cameraRenderState.pos;
        pose.translate(-camPos.x(), -camPos.y(), -camPos.z());

        StreetArtClient.textureManager.forEach(data -> {
            renderGraffiti(storage, pose, data);
        });
    }

    private static void renderGraffiti(SubmitNodeStorage storage, PoseStack pose, GraffitiManager.TileData data) {
        pose.pushPose();
        pose.translate(data.pos.x, data.pos.y, data.pos.z);
        pose.rotateAround(data.dir.getRotation(), 0.5f, 0.5f, 0.5f);
        storage.submitCustomGeometry(pose, RenderTypes.entityCutout(data.tile.location), GraffitiRenderer::renderDecal);
        pose.popPose();
    }

    private static void renderDecal(PoseStack.Pose pose, VertexConsumer buffer) {
        vertex(pose, buffer, 0, 0);
        vertex(pose, buffer, 0, 1);
        vertex(pose, buffer, 1, 1);
        vertex(pose, buffer, 1, 0);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float z) {
        buffer.addVertex(pose, x, 1, z)
                .setColor(-1)
                .setUv(x, z)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
