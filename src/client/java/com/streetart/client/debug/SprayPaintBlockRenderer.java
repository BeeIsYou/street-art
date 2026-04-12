package com.streetart.client.debug;

import com.streetart.client.StreetArtClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;

public class SprayPaintBlockRenderer implements DebugRenderer.SimpleDebugRenderer {

    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        StreetArtClient.textureManager.getGraffiti().keySet().forEach(block -> {
            Gizmos.cuboid(
                    new AABB(block).inflate(0.05),
                    GizmoStyle.fill(ARGB.colorFromFloat(0.5f, 1, 1, 1))
            );
        });
    }
}
