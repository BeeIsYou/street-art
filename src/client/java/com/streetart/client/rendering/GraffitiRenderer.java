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
import com.streetart.AllGraffitiLayers;
import com.streetart.StreetArtConfig;
import com.streetart.client.StreetArtClient;
import com.streetart.client.manager.GClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.function.Function;

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
    public static final Function<Identifier, RenderType> GRAFFITI_TYPE;
    static {
        GRAFFITI_TYPE = Util.memoize(identifier -> RenderType.create("street_art:graffiti",
                RenderSetup.builder(TRANSLUCENT_GRAFFITI)
                    .useLightmap()
                    .withTexture(
                            "Sampler0",
                            identifier,
                            () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST, true)
                    )
                    .sortOnUpload()
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
        ));
    }

    public static void render(final SubmitNodeStorage storage, Vec3 camPos) {
        if (StreetArtConfig.ignoreEverything()) {
            return;
        }

        final PoseStack pose = new PoseStack();
        pose.translate(-camPos.x(), -camPos.y(), -camPos.z());

        final Player player = Minecraft.getInstance().player;
        final Level level = Minecraft.getInstance().level;

        final Identifier active = AllGraffitiLayers.getActive(player, level).identifier();

        // todo layer ordering
        StreetArtClient.layers.forEach((identifier, atlas) -> {
            if (atlas.isActive(Minecraft.getInstance().player, Minecraft.getInstance().level)) {
                final boolean opaque = active.equals(identifier);
                final int mask = opaque ? 0xFFFFFFFF : 0x3FFFFFFF;
                storage.order(-1).submitCustomGeometry(pose, GRAFFITI_TYPE.apply(identifier), (_pose, buffer) -> {
                    final PoseStack.Pose original = _pose.copy();
                    atlas.forEach((_, manager) -> {
                        manager.forEach(data -> renderGraffiti(_pose, original, camPos, buffer, atlas, data, mask));
                    });
                });
            }
        });
    }

    private static final Vector4f mutUV = new Vector4f();
    private static void renderGraffiti(final PoseStack.Pose pose, final PoseStack.Pose original, final Vec3 camPos, final VertexConsumer buffer, final GraffitiAtlas tileAtlasManager, final GClientData data, final int colorMask) {
        if (data.light0 != -1) {

            final float depthOff = 1 - data.depth / 16f;

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
                    data.color0 & colorMask, data.color1 & colorMask, data.color2 & colorMask, data.color3 & colorMask
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
