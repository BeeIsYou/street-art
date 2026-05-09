package com.streetart.client.manager;

import com.streetart.*;
import com.streetart.client.StreetArtClient;
import com.streetart.component.paint_placer.PaintPlacerComponent;
import com.streetart.component.paint_placer.Spray;
import com.streetart.networking.BiDirectionalGraffitiChange;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
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
import java.util.Map;

public class SpraySessionManager {
    public static boolean active = false;
    private static final List<SpraySnapshot> positionSnapshots = new ArrayList<>();
    private static final Map<Identifier, Int2ObjectMap<BiDirectionalGraffitiChange>> layerChanges = new HashMap<>();

    public static void takeSnapshot(final Player player) {
        if (active) {
            positionSnapshots.add(new SpraySnapshot(
                    player.getEyePosition(),
                    new Vec2(player.getXRot(), player.getYRot()))
            );
        }
    }

    public static void tick(final Minecraft minecraft) {
        if (StreetArtConfig.ignoreEverything()) {
            positionSnapshots.clear();
            layerChanges.clear();
            return;
        }

        final LocalPlayer player = minecraft.player;
        if (player == null) {
            active = false;
            positionSnapshots.clear();
            return;
        }

        final ItemStack stack = player.getUseItem();
        final PaintPlacerComponent placer = stack.get(AllDataComponents.PAINT_PLACER);

        if (placer != null) {
            final boolean rightClick = minecraft.options.keyUse.isDown();
            final Spray spray = placer.getSpray(rightClick);
            if (spray != null) {
                performSpray(player, minecraft, stack, spray);
            } else {
                active = false;
                positionSnapshots.clear();
            }
        } else {
            active = false;
            positionSnapshots.clear();
        }

        if (minecraft.getConnection() != null) {
            trySendServerUpdate();
        } else {
            layerChanges.clear();
        }
    }

    private static void performSpray(final Player player, final Minecraft minecraft, final ItemStack stack, final Spray spray) {
        final Identifier activeLayer = AllGraffitiLayers.getActive(player, minecraft.level).identifier();

        active = true;
        final Int2ObjectMap<BiDirectionalGraffitiChange> activeLayerChanges =
                SpraySessionManager.layerChanges.computeIfAbsent(activeLayer, _ -> new Int2ObjectOpenHashMap<>());
        final BiDirectionalGraffitiChange change = activeLayerChanges.computeIfAbsent(
                spray.color().id, _ -> BiDirectionalGraffitiChange.create(activeLayer, spray.color())
        );

        takeSnapshot(player);
        boolean madeParticle = false;
        for (int i = 0; i < spray.iterations(); i++) {
            final float pt = (float) i / spray.iterations();

            SpraySnapshot snapshot = sampleLerp(pt);
            if (snapshot == null) {
                snapshot = new SpraySnapshot(player.getEyePosition(), new Vec2(player.getXRot(), player.getYRot()));
            }

            final Vec3 originalView = player.calculateViewVector(snapshot.look.x, snapshot.look.y);

            final Vec3 view = spray.getLookVector(player, snapshot.look, originalView, pt);

            final double range = player.blockInteractionRange();
            final Vec3 to = snapshot.pos.add(view.x * range, view.y * range, view.z * range);
            final BlockHitResult hitResult = player.level().clip(new ClipContext(snapshot.pos, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

            if (hitResult.getType() == HitResult.Type.BLOCK &&
                    PermissionUtil.sprayingAllowed(hitResult.getBlockPos(), player.level(), stack, player, activeLayer)) {
                final Vector2i coordinates = ArtUtil.calculatePixelCoordinates(hitResult);

                final GClientManager man = StreetArtClient.layers.get(activeLayer).getOrCreate(hitResult.getBlockPos());
                if (man.applyPixelChangeAndLight(hitResult, coordinates, spray.color().argb, minecraft.level)) {
                    change.markChanged(hitResult, coordinates.x, coordinates.y);
                }

                if (!madeParticle) {
                    madeParticle = true;
                    final ParticleOptions particle = spray.getParticle();
                    if (particle != null) {
                        player.level().addParticle(particle,
                                hitResult.getLocation().x - view.x * 0.2,
                                hitResult.getLocation().y - view.y * 0.2,
                                hitResult.getLocation().z - view.z * 0.2,
                                0, 0, 0
                        );
                    }
                }
            }
        }

        positionSnapshots.clear();
        takeSnapshot(player);
    }

    public static void trySendServerUpdate() {
        layerChanges.forEach((_, layerChanges) -> {
            layerChanges.forEach((_, change) -> ClientPlayNetworking.send(change));
        });
        layerChanges.clear();
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
