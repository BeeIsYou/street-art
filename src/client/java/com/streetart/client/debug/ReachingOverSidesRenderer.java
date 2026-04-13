package com.streetart.client.debug;

import com.streetart.AttachmentTypes;
import com.streetart.managers.GServerChunkManager;
import com.streetart.mixin.ChunkMapInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;

public class ReachingOverSidesRenderer implements DebugRenderer.SimpleDebugRenderer {
    @Override
    public void emitGizmos(final double camX, final double camY, final double camZ, final DebugValueAccess debugValues, final Frustum frustum, final float partialTicks) {
        final Minecraft minecraft = Minecraft.getInstance();
        final IntegratedServer server = minecraft.getSingleplayerServer();
        if (server != null && minecraft.level != null) {
            final ServerLevel level = server.getLevel(minecraft.level.dimension());
            // todo race condition?
            ((ChunkMapInvoker)level.getChunkSource().chunkMap).callForEachBlockTickingChunk(chunk -> {
                final GServerChunkManager manager = chunk.getAttached(AttachmentTypes.CHUNK_MANAGER);
                if (manager != null) {
                    manager.getGraffiti().keySet().forEach(block -> {
                        Gizmos.cuboid(
                            new AABB(block).inflate(0.05),
                            GizmoStyle.fill(ARGB.colorFromFloat(0.5f, 1, 0, 0))
                        );
                    });
                }
            });
        }
    }
}
