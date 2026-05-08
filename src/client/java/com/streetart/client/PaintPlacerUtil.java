package com.streetart.client;

import com.streetart.component.paint_placer.PaintPlacerComponent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class PaintPlacerUtil {
    public static void throwParticles(final Level level, final Player player, final PaintPlacerComponent placer) {
        final ParticleOptions particle;
        if (placer.rightClick().isPresent()) {
            particle = placer.rightClick().get().getParticle();
        } else if (placer.leftClick().isPresent()) {
            particle = placer.leftClick().get().getParticle();
        } else {
            return;
        }

        final Vec3 origin = getParticleOrigin(
                placer,
                player,
                Minecraft.getInstance().options,
                Minecraft.getInstance().gameRenderer.getMainCamera()
        );
        final Vec3 look = getParticleDirection(player);

        level.addParticle(particle, origin.x, origin.y, origin.z, look.x, look.y, look.z);
    }

    // mostly stolen from FishingHookRenderer
    public static Vec3 getParticleOrigin(final PaintPlacerComponent placer, final Player player, final Options options, final Camera camera) {
        final boolean isMain = player.getUsedItemHand() == InteractionHand.MAIN_HAND;
        final boolean isLeftHanded = player.getMainArm() == HumanoidArm.LEFT;
        final int invert = isMain ^ isLeftHanded ? 1 : -1;
        if (options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            final float fov = options.fov().get();
            final double viewBobbingScale = 960.0 / fov;
            final float swing = player.getAttackAnim(1);
            final float swing2 = Mth.sin(Mth.sqrt(swing) * (float) Math.PI);
            final Vector3f plane = placer.firstPersonOffset().toVector3f();
            final Vec3 viewVec = camera
                    .getNearPlane(fov)
                    .getPointOnPlane(invert * plane.x, plane.y)
                    .scale(viewBobbingScale * plane.z)
                    .yRot(swing2 * 0.5F)
                    .xRot(-swing2 * 0.7F);
            return player.getEyePosition(1).add(viewVec);
        } else {
            final float ownerYRot = (player.yHeadRot - 5) * (float) (Math.PI / 180.0);
            final double sin = Mth.sin(ownerYRot);
            final double cos = Mth.cos(ownerYRot);
            final float playerScale = player.getScale();
            final Vec2 offsets = placer.thirdPersonOffset();
            final float yOffset = player.isCrouching() ? -0.1875F : 0.0F;
            return player.getEyePosition(1).add(
                (-cos * offsets.x - sin * offsets.y) * playerScale,
                yOffset - 0.45 * playerScale,
                (-sin * offsets.x + cos * offsets.y) * playerScale
            );
        }
    }

    static Vec3 getParticleDirection(final Player player) {
        return player.calculateViewVector(
                player.getXRot(),
                player.getYRot() - 10
        ).scale(0.3);
    }
}
