package com.streetart.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ArtUtil {
    // mostly stolen from FishingHookRenderer
    public static Vec3 getParticleOrigin(Player player, Options options, Camera camera) {
        boolean isMain = player.getUsedItemHand() == InteractionHand.MAIN_HAND;
        boolean isLeftHanded = player.getMainArm() == HumanoidArm.LEFT;
        int invert = isMain ^ isLeftHanded ? 1 : -1;
        if (options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            float fov = options.fov().get();
            double viewBobbingScale = 960.0 / fov;
            float swing = player.getAttackAnim(1);
            float swing2 = Mth.sin(Mth.sqrt(swing) * (float) Math.PI);
            Vec3 viewVec = camera
                    .getNearPlane(fov)
                    .getPointOnPlane(invert * 0.525F, -0.1F)
                    .scale(viewBobbingScale)
                    .yRot(swing2 * 0.5F)
                    .xRot(-swing2 * 0.7F);
            return player.getEyePosition(1).add(viewVec);
        } else {
            float ownerYRot = (player.yHeadRot - 5) * (float) (Math.PI / 180.0);;
            double sin = Mth.sin(ownerYRot);
            double cos = Mth.cos(ownerYRot);
            float playerScale = player.getScale();
            double rightOffset = invert * 0.35 * playerScale;
            double forwardOffset = 0.8 * playerScale;
            float yOffset = player.isCrouching() ? -0.1875F : 0.0F;
            return player.getEyePosition(1)
                    .add(-cos * rightOffset - sin * forwardOffset, yOffset - 0.45 * playerScale, -sin * rightOffset + cos * forwardOffset);
        }
    }

    public static Vec3 getParticleDirection(Player player) {
        return player.calculateViewVector(
                player.getXRot(),
                player.getYRot() - 10
        ).scale(0.3);
    }
}
