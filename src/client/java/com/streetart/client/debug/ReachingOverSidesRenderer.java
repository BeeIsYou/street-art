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
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server != null && minecraft.level != null) {
            ServerLevel level = server.getLevel(minecraft.level.dimension());
            ((ChunkMapInvoker)level.getChunkSource().chunkMap).callForEachBlockTickingChunk(chunk -> {
                GServerChunkManager manager = chunk.getAttached(AttachmentTypes.CHUNK_MANAGER);
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
