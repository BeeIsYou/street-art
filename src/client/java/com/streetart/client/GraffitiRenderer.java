package com.streetart.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.streetart.client.manager.GClientManager;
import com.streetart.client.mixin.LevelRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

    private static void renderGraffiti(SubmitNodeStorage storage, PoseStack pose, GClientManager.TileData data) {
        pose.pushPose();
        pose.translate(data.pos.x, data.pos.y, data.pos.z);
        pose.rotateAround(data.dir.getRotation(), 0.5f, 0.5f, 0.5f);
        int l = data.tile.light;
        storage.submitCustomGeometry(pose, RenderTypes.entityCutout(data.tile.location),
                (_pose, buffer) -> GraffitiRenderer.renderDecal(_pose, buffer, l)
        );
        pose.popPose();
    }

    private static void renderDecal(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        vertex(pose, buffer, 0, 0, light);
        vertex(pose, buffer, 0, 1, light);
        vertex(pose, buffer, 1, 1, light);
        vertex(pose, buffer, 1, 0, light);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float z, int light) {
        buffer.addVertex(pose, x, 1, z)
                .setColor(-1)
                .setUv(x, z)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
