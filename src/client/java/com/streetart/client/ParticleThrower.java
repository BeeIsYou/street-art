package com.streetart.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public interface ParticleThrower {
    ParticleOptions getParticleOptions(Player player, ItemStack itemStack);

    default void throwParticles(Level level, Player player, ItemStack itemStack) {
        Vec3 origin = getParticleOrigin(
                this,
                player,
                Minecraft.getInstance().options,
                Minecraft.getInstance().gameRenderer.getMainCamera()
        );
        Vec3 look = getParticleDirection(player);

        level.addParticle(this.getParticleOptions(player, itemStack),
                origin.x, origin.y, origin.z, look.x, look.y, look.z
        );
    }

    /** xy will get scaled by z */
    Vector3f firstPersonPlane();
    /** x -> right, y -> forwards */
    Vec2 thirdPersonOffset();

    // mostly stolen from FishingHookRenderer
    static Vec3 getParticleOrigin(ParticleThrower self, Player player, Options options, Camera camera) {
        boolean isMain = player.getUsedItemHand() == InteractionHand.MAIN_HAND;
        boolean isLeftHanded = player.getMainArm() == HumanoidArm.LEFT;
        int invert = isMain ^ isLeftHanded ? 1 : -1;
        if (options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            float fov = options.fov().get();
            double viewBobbingScale = 960.0 / fov;
            float swing = player.getAttackAnim(1);
            float swing2 = Mth.sin(Mth.sqrt(swing) * (float) Math.PI);
            Vector3f plane = self.firstPersonPlane();
            Vec3 viewVec = camera
                    .getNearPlane(fov)
                    .getPointOnPlane(invert * plane.x, plane.y)
                    .scale(viewBobbingScale * plane.z)
                    .yRot(swing2 * 0.5F)
                    .xRot(-swing2 * 0.7F);
            return player.getEyePosition(1).add(viewVec);
        } else {
            float ownerYRot = (player.yHeadRot - 5) * (float) (Math.PI / 180.0);;
            double sin = Mth.sin(ownerYRot);
            double cos = Mth.cos(ownerYRot);
            float playerScale = player.getScale();
            Vec2 offsets = self.thirdPersonOffset();
            float yOffset = player.isCrouching() ? -0.1875F : 0.0F;
            return player.getEyePosition(1).add(
                (-cos * offsets.x - sin * offsets.y) * playerScale,
                yOffset - 0.45 * playerScale,
                (-sin * offsets.x + cos * offsets.y) * playerScale
            );
        }
    }

    static Vec3 getParticleDirection(Player player) {
        return player.calculateViewVector(
                player.getXRot(),
                player.getYRot() - 10
        ).scale(0.3);
    }
}
