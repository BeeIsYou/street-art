package com.streetart.client.manager;

import com.streetart.SprayPaintInteractor;
import com.streetart.client.StreetArtClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SpraySessionManager {
    public static boolean active = false;
    private static List<Vec2> lookXY = new ArrayList<>();

    public static void playerTurned(Player player) {
        if (active) {
            lookXY.add(new Vec2(player.getXRot(), player.getYRot()));
        }
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            active = false;
            lookXY.clear();
            return;
        }
        ItemStack stack = player.getUseItem();
        if (stack.getItem() instanceof SprayPaintInteractor sprayPaint && sprayPaint.hasColor(player, stack)) {
            active = true;

            int iterations = sprayPaint.iterationsPerTick(player, stack);
            Vec3 from = player.getEyePosition();
            for (int i = 0; i < iterations; i++) {
                float pt = (float) i / iterations;

                Vec2 xyRot = sampleLerp(pt, new Vec2(player.getXRot(), player.getYRot()));
                Vec3 originalView = player.calculateViewVector(xyRot.x, xyRot.y);

                Vec3 view = sprayPaint.getLookVector(player, xyRot, originalView, stack, pt);

                double range = player.blockInteractionRange();
                Vec3 to = from.add(view.x * range, view.y * range, view.z * range);
                BlockHitResult hitResult = player.level().clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    StreetArtClient.textureManager.computeChanges(hitResult, sprayPaint.getColor(player, stack));
                }
            }
            lookXY.clear();
            lookXY.add(new Vec2(player.getXRot(), player.getYRot()));
        } else {
            active = false;
            lookXY.clear();
        }
    }

    private static Vec2 sampleLerp(float pt, Vec2 fallBack) {
        if (lookXY.isEmpty()) {
            return fallBack;
        }
        if (lookXY.size() == 1) {
            return lookXY.getFirst();
        }
        float ipt = pt * lookXY.size();
        int i = Mth.floor(ipt);
        if (i >= lookXY.size() - 1) {
            return lookXY.getLast();
        }
        float mix = (ipt - i);
        return lookXY.get(i).scale(1 - mix).add(lookXY.get(i + 1).scale(mix));
    }
}
