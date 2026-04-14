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
    public static void render(final LevelTerrainRenderContext context) {
        final SubmitNodeStorage storage = ((LevelRendererAccessor)context.levelRenderer()).getSubmitNodeStorage();

        final PoseStack pose = new PoseStack();
        final Vec3 camPos = context.levelState().cameraRenderState.pos;
        pose.translate(-camPos.x(), -camPos.y(), -camPos.z());

        final RenderType graffitiAtlas = RenderTypes.entityCutout(StreetArtClient.textureManager.tileAtlasManager.atlasLocation);
        StreetArtClient.textureManager.forEach(data -> {
            renderGraffiti(storage, pose, graffitiAtlas, data);
        });
    }

    private static void renderGraffiti(final SubmitNodeStorage storage, final PoseStack pose,
                                       final RenderType graffitiAtlas, final GClientData data) {
        if (data.light0 != -1) {
            pose.pushPose();
            pose.translate(
                    data.pos.getX() + data.dir.getStepX() * data.getDepth(),
                    data.pos.getY() + data.dir.getStepY() * data.getDepth(),
                    data.pos.getZ() + data.dir.getStepZ() * data.getDepth()
            );
            pose.rotateAround(data.dir.getRotation(), 0.5f, 0.5f, 0.5f);
            final Vector2f uv = TileAtlasManager.getUV(data.id);
            storage.submitCustomGeometry(pose, graffitiAtlas,
                    (_pose, buffer) -> GraffitiRenderer.renderDecal(
                            _pose, buffer,
                            uv.x, uv.y,
                            uv.x + TileAtlasManager.U_SIZE, uv.y + TileAtlasManager.V_SIZE,
                            data.light0, data.light1, data.light2, data.light3,
                            data.color0, data.color1, data.color2, data.color3
                    )
            );
            pose.popPose();
        }
    }

    private static void renderDecal(final PoseStack.Pose pose, final VertexConsumer buffer,
                                    final float u1, final float v1, final float u2, final float v2,
                                    final int light0, final int light1, final int light2, final int light3,
                                    final int color0, final int color1, final int color2, final int color3
    ) {
        vertex(pose, buffer, 0, 0, u1, v1, light0, color0);
        vertex(pose, buffer, 0, 1, u1, v2, light1, color1);
        vertex(pose, buffer, 1, 1, u2, v2, light2, color2);
        vertex(pose, buffer, 1, 0, u2, v1, light3, color3);
    }

    private static void vertex(final PoseStack.Pose pose, final VertexConsumer buffer, final float x, final float z, final float u, final float v, final int light, final int color) {
        buffer.addVertex(pose, x, 0.01f, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
