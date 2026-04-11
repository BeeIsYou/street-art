package com.streetart.client.manager;

import com.streetart.AllItems;
import com.streetart.client.StreetArtClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpraySessionManager {
    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getUseItem();
        if (stack.is(AllItems.SPRAY_CAN) && stack.has(DataComponents.DYED_COLOR)) {
            for (int i = 0; i < 16; i++) {
                Vec3 view = player.calculateViewVector(player.getXRot(i / 16f), player.getYRot(i / 16f));

                double a1 = player.getRandom().nextGaussian() * 0.05f;
                double a2 = player.getRandom().nextGaussian() * 0.05f;

                Vec3 rotated1 = new Vec3(
                        view.x * Math.cos(a1) + view.z * -Math.sin(a1),
                        view.y,
                        view.x * Math.sin(a1) + view.z * Math.cos(a1)
                );
                Vec3 rotated2 = new Vec3(
                        rotated1.x,
                        rotated1.y * Math.cos(a2) + rotated1.z * -Math.sin(a2),
                        rotated1.y * Math.sin(a2) + rotated1.z * Math.cos(a2)
                );

                double range = player.blockInteractionRange();
                Vec3 from = player.getEyePosition();
                Vec3 to = from.add(rotated2.x * range, rotated2.y * range, rotated2.z * range);
                BlockHitResult hitResult = player.level().clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    StreetArtClient.textureManager.computeChanges(hitResult, stack.get(DataComponents.DYED_COLOR).rgb());
                }
            }
        }
    }
}
