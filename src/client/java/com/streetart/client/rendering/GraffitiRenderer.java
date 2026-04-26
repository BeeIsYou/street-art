package com.streetart.client.rendering;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.streetart.client.StreetArtClient;
import com.streetart.client.manager.GClientData;
import com.streetart.client.manager.GClientManager;
import com.streetart.client.mixin.LevelRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

public class GraffitiRenderer {
    public static final DepthStencilState DEPTH_STENCIL = new DepthStencilState(
            CompareOp.LESS_THAN_OR_EQUAL, true,
            -1, -10
    );
    public static final RenderPipeline TRANSLUCENT_GRAFFITI = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
                    .withLocation("pipeline/translucent_block")
                    .withShaderDefine("ALPHA_CUTOUT", 0.01F)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(DEPTH_STENCIL)
                    .withCull(false)
                    .build());
    public static final RenderType GRAFFITI_TYPE;
    static {
        final RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(TRANSLUCENT_GRAFFITI)
                .useLightmap()
                .withTexture(
                        "Sampler0",
                        StreetArtClient.tileAtlasManager.atlasLocation,
                        () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST, true)
                )
                .sortOnUpload()
                .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET);
        GRAFFITI_TYPE = RenderType.create("street_art:graffiti", setup.createRenderSetup());
    }

    public static void render(final LevelTerrainRenderContext context) {
        final SubmitNodeStorage storage = ((LevelRendererAccessor)context.levelRenderer()).getSubmitNodeStorage();

        final PoseStack pose = new PoseStack();
        final Vec3 camPos = context.levelState().cameraRenderState.pos;
        pose.translate(-camPos.x(), -camPos.y(), -camPos.z());

        storage.submitCustomGeometry(pose, GRAFFITI_TYPE, (_pose, buffer) -> {
            final PoseStack.Pose original = _pose.copy();
            for (final GClientManager entry : StreetArtClient.textureManager.values()) {
                entry.forEach(data -> renderGraffiti(_pose, original, camPos, buffer, StreetArtClient.tileAtlasManager, data));
            }
        });
    }

    private static final Vector4f mutUV = new Vector4f();
    private static void renderGraffiti(final PoseStack.Pose pose, final PoseStack.Pose original, final Vec3 camPos, final VertexConsumer buffer, final TileAtlasManager tileAtlasManager, final GClientData data) {
        if (data.light0 != -1) {
            final float depthOff = (float) (data.getDepth());

            pose.translate(
                    data.pos.getX() + data.dir.getStepX() * depthOff,
                    data.pos.getY() + data.dir.getStepY() * depthOff,
                    data.pos.getZ() + data.dir.getStepZ() * depthOff
            );

            pose.rotateAround(data.dir.getRotation(), 0.5f, 0.5f, 0.5f);
            tileAtlasManager.writeUVs(data.id, mutUV);
            GraffitiRenderer.renderDecal(
                    pose, buffer,
                    mutUV.x, mutUV.y,
                    mutUV.z, mutUV.w,
                    data.light0, data.light1, data.light2, data.light3,
                    data.color0, data.color1, data.color2, data.color3
            );
            pose.set(original);
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
        buffer.addVertex(pose, x, 0f, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
