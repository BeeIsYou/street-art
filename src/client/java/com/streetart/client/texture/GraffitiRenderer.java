package com.streetart.client.texture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.streetart.client.StreetArtClient;
import com.streetart.client.manager.GClientData;
import com.streetart.client.mixin.LevelRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

public class GraffitiRenderer {
    public static void render(LevelTerrainRenderContext context) {
        SubmitNodeStorage storage = ((LevelRendererAccessor)context.levelRenderer()).getSubmitNodeStorage();

        PoseStack pose = new PoseStack();
        Vec3 camPos = context.levelState().cameraRenderState.pos;
        pose.translate(-camPos.x(), -camPos.y(), -camPos.z());

        RenderType graffitiAtlas = RenderTypes.entityCutout(StreetArtClient.textureManager.tileAtlasManager.atlasLocation);
        StreetArtClient.textureManager.forEach(data -> {
            renderGraffiti(storage, pose, graffitiAtlas, data);
        });
    }

    private static void renderGraffiti(SubmitNodeStorage storage, PoseStack pose,
                                       RenderType graffitiAtlas, GClientData data) {
        if (data.light != -1) {
            pose.pushPose();
            pose.translate(
                    data.pos.getX() + data.dir.getStepX() * data.depth,
                    data.pos.getY() + data.dir.getStepY() * data.depth,
                    data.pos.getZ() + data.dir.getStepZ() * data.depth
            );
            pose.rotateAround(data.dir.getRotation(), 0.5f, 0.5f, 0.5f);
            Vector2f uv = TileAtlasManager.getUV(data.id);
            storage.submitCustomGeometry(pose, graffitiAtlas,
                    (_pose, buffer) -> GraffitiRenderer.renderDecal(
                            _pose, buffer,
                            uv.x, uv.y,
                            uv.x + TileAtlasManager.U_SIZE, uv.y + TileAtlasManager.V_SIZE,
                            data.light
                    )
            );
            pose.popPose();
        }
    }

    private static void renderDecal(PoseStack.Pose pose, VertexConsumer buffer,
                                    float u1, float v1, float u2, float v2, int light) {
        vertex(pose, buffer, 0, 0, u1, v1, light);
        vertex(pose, buffer, 0, 1, u1, v2, light);
        vertex(pose, buffer, 1, 1, u2, v2, light);
        vertex(pose, buffer, 1, 0, u2, v1, light);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float z, float u, float v, int light) {
        buffer.addVertex(pose, x, 0.01f, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
