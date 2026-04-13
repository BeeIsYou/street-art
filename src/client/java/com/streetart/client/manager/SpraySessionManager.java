package com.streetart.client.manager;

import com.streetart.ArtUtil;
import com.streetart.StreetArt;
import com.streetart.client.StreetArtClient;
import com.streetart.item.SprayPaintInteractor;
import com.streetart.networking.BiDirectionalGraffitiChange;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpraySessionManager {
    public static boolean active = false;
    private static final List<SpraySnapshot> positionSnapshots = new ArrayList<>();
    private static final Int2ObjectMap<BiDirectionalGraffitiChange> changes = new Int2ObjectOpenHashMap<>();

    public static void takeSnapshot(final Player player) {
        if (active) {
            positionSnapshots.add(new SpraySnapshot(
                    player.getEyePosition(),
                    new Vec2(player.getXRot(), player.getYRot()))
            );
        }
    }

    public static void tick(final Minecraft minecraft) {
        final LocalPlayer player = minecraft.player;
        if (player == null) {
            active = false;
            positionSnapshots.clear();
            return;
        }

        final ItemStack stack = player.getUseItem();
        if (stack.getItem() instanceof final SprayPaintInteractor sprayPaint && sprayPaint.hasColor(player, stack)) {
            active = true;
            final int color = sprayPaint.getColor(player, stack);
            final BiDirectionalGraffitiChange change = changes.computeIfAbsent(color, _ -> new BiDirectionalGraffitiChange(color, new HashMap<>()));

            final boolean rightClick = minecraft.options.keyUse.isDown();
            final int iterations = sprayPaint.iterationsPerTick(player, stack);

            boolean madeParticle = false;
            for (int i = 0; i < iterations; i++) {
                final float pt = (float) i / iterations;

                SpraySnapshot snapshot = sampleLerp(pt);
                if (snapshot == null) {
                    snapshot = new SpraySnapshot(player.getEyePosition(), new Vec2(player.getXRot(), player.getYRot()));
                }

                final Vec3 originalView = player.calculateViewVector(snapshot.look.x, snapshot.look.y);

                final Vec3 view = sprayPaint.getLookVector(player, snapshot.look, originalView, stack, pt, rightClick);

                final double range = player.blockInteractionRange();
                final Vec3 to = snapshot.pos.add(view.x * range, view.y * range, view.z * range);
                final BlockHitResult hitResult = player.level().clip(new ClipContext(snapshot.pos, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

                if (hitResult.getType() == HitResult.Type.BLOCK &&
                        StreetArt.AREA_LIB.allowedToEdit(player, hitResult.getBlockPos())) {
                    final Vector2i coordinates = ArtUtil.calculatePixelCoordinates(hitResult);
                    if (StreetArtClient.textureManager.applyPixelChange(hitResult, coordinates, color)) {
                        change.markChanged(hitResult, coordinates.x, coordinates.y);
                    }
                    if (!madeParticle) {
                        madeParticle = true;
                        player.level().addParticle(sprayPaint.getParticleAtPoint(player, stack),
                                hitResult.getLocation().x - view.x * 0.2,
                                hitResult.getLocation().y - view.y * 0.2,
                                hitResult.getLocation().z - view.z * 0.2,
                                0, 0, 0
                        );
                    }
                }
            }

            positionSnapshots.clear();
            takeSnapshot(player);
        } else {
            active = false;
            positionSnapshots.clear();
        }
    }

    public static void sync() {
        changes.forEach((_, change) -> ClientPlayNetworking.send(change));
        changes.clear();
    }

    private static @Nullable SpraySnapshot sampleLerp(final float pt) {
        if (positionSnapshots.isEmpty()) {
            return null;
        }
        if (positionSnapshots.size() == 1) {
            return positionSnapshots.getFirst();
        }
        final float ipt = pt * positionSnapshots.size();
        final int i = Mth.floor(ipt);
        if (i >= positionSnapshots.size() - 1) {
            return positionSnapshots.getLast();
        }
        final float mix = (ipt - i);
        final Vec2 lookA = positionSnapshots.get(i).look;
        final Vec2 lookB = positionSnapshots.get(i+1).look;
        final Vec3 posA = positionSnapshots.get(i).pos;
        final Vec3 posB = positionSnapshots.get(i+1).pos;
        return new SpraySnapshot(
                posA.scale(1 - mix).add(posB.scale(mix)),
                lookA.scale(1 - mix).add(lookB.scale(mix))
        );
    }

    private record SpraySnapshot(Vec3 pos, Vec2 look) {}
}
