package com.streetart.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.AllGameRules;
import com.streetart.StreetArt;
import com.streetart.arealib.AreaLib;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.TileChange;
import com.streetart.graffiti_data.TileKey;
import com.streetart.managers.data.GServerBlock;
import com.streetart.managers.data.GServerDataHolder;
import com.streetart.managers.data.TempData;
import com.streetart.managers.public_facing_interfaces.PublicFacingBlockData;
import com.streetart.networking.BiDirectionalGraffitiChange;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ClientBoundInvalidateBlock;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GServerChunkManager {

    public static final Codec<GServerChunkManager> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.list(GServerBlock.CODEC).fieldOf("block_data").forGetter(manager -> manager.graffiti.values().stream().toList()))
            .apply(i, GServerChunkManager::new)
    );

    private final Map<BlockPos, GServerBlock> graffiti;
    private final List<TempData> dirtyData = new ArrayList<>();
    private final List<BiDirectionalGraffitiChange> patches = new ArrayList<>();

    public GServerChunkManager() {
        this.graffiti = new HashMap<>();
    }

    public GServerChunkManager(final List<GServerBlock> gServerBlocks) {
        this.graffiti = new HashMap<>();
        for (final GServerBlock b : gServerBlocks) {
            this.graffiti.put(b.getBlockPos(), b);
        }
    }

    public boolean tick(final ServerLevel level, final ChunkPos pos) {
        this.tickDecay(level, pos);

        final boolean shouldSaveData = !this.dirtyData.isEmpty();

        this.dirtyData.removeIf(tempData -> {
            final Type type = tempData.type();

            CustomPacketPayload packet = null;
            switch (type) {
                case FULL_RESEND -> {
                    packet = new ClientBoundGraffitiSet(
                            tempData.pos(),
                            tempData.dir(),
                            tempData.data().getDepth(),
                            tempData.data().getGraffitiData().array());
                }

                case REMOVED -> {
                    this.graffiti.remove(tempData.pos());
                    packet = new ClientBoundInvalidateBlock(tempData.pos());
                }

                case SMOTHERED -> {
                    final GServerBlock block = this.graffiti.get(tempData.pos());

                    block.removeHolder(tempData.dir(), tempData.data());
                    packet = new ClientBoundGraffitiSet(
                            tempData.pos(),
                            tempData.dir(),
                            tempData.data().getDepth(),
                            new byte[0]);
                }
            }

            for (final ServerPlayer player : PlayerLookup.tracking(level, pos)) {
                ServerPlayNetworking.send(player, packet);
            }

            return true;
        });

        this.patches.removeIf(patch -> {
            for (final ServerPlayer player : PlayerLookup.tracking(level, pos)) {
                ServerPlayNetworking.send(player, patch);
            }
            return true;
        });

        return shouldSaveData;
    }

    public <T extends GServerBlock & PublicFacingBlockData> Iterable<T> allGraffitiBlockData() {
        return (Iterable<T>) this.graffiti.values();
    }

    public Iterable<BlockPos> allGraffitiBlocksPositions() {
        return this.graffiti.keySet();
    }

    public void markDirty(final GServerDataHolder data, final BlockPos pos, final Direction dir) {
        this.dirtyData.add(new TempData(data, pos, dir, Type.FULL_RESEND));
    }

    public void markForRemoval(final BlockPos pos) {
        if (this.graffiti.containsKey(pos)) {
            this.dirtyData.add(new TempData(null, pos, null, Type.REMOVED));
        }
    }

    public void markSmothered(final BlockPos pos, final Direction dir) {
        final GServerBlock block = this.graffiti.get(pos);
        if (block != null) {
            block.handleSmothered(this.dirtyData, dir);
        }
    }

    public void addPatch(final BiDirectionalGraffitiChange patch) {
        this.patches.add(patch);
    }

    public void tickDecay(final ServerLevel level, final ChunkPos chunkPos) {
        final int decaySpeed = level.getGameRules().get(AllGameRules.RANDOM_DECAY_SPEED);
        for (int i = 0; i < level.getSectionsCount(); i++) {
            final int sectionY = level.getSectionYFromSectionIndex(i);
            for (int j = 0; j < decaySpeed; j++) {
                final BlockPos randomPos = level.getBlockRandomPos(
                        chunkPos.getMinBlockX(),
                        SectionPos.sectionToBlockCoord(sectionY),
                        chunkPos.getMinBlockZ(),
                        15
                );

                if (!StreetArt.AREA_LIB.isInRegion(level, randomPos, AreaLib.Type.NO_DECAY)) {
                    final GServerBlock block = this.graffiti.get(randomPos);
                    if (block != null) {
                        if (block.randomDecay(level)) {
                            this.markForRemoval(randomPos);
                        } else {
                            for (final Direction dir : block.allValidDirections()) {
                                final Iterable<GServerDataHolder> holders = block.dataFromDir(dir);
                                if (holders != null) {
                                    for (final GServerDataHolder holder : holders) {
                                        this.markDirty(holder, randomPos, dir);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void handleRequest(final ServerPlayNetworking.Context context) {
        for (final GServerBlock value : this.graffiti.values()) {
            for (final Direction direction : value.allValidDirections()) {

                final Iterable<GServerDataHolder> holders = value.dataFromDir(direction);
                if (holders != null) {
                    for (final GServerDataHolder holder : holders) {
                        ServerPlayNetworking.send(context.player(), new ClientBoundGraffitiSet(
                                value.getBlockPos(),
                                direction,
                                holder.getDepth(),
                                holder.getGraffitiData().array()
                        ));
                    }
                }
            }
        }
    }

    public boolean handleChange(final BiDirectionalGraffitiChange packet, final TileKey key, final TileChange change) {
        final GServerDataHolder tile = this.getOrConditionalCreate(
                key.pos(),
                key.dir(),
                key.depth(),
                packet.content() == ColorComponent.CLEAR.id
        );

        if (tile == null) {
            return false;
        }

        if (tile.handleChange(packet.content(), change)) {
            this.tryRemoveData(key.pos(), key.dir(), key.depth());
        }

        if (packet.content() != 0) {
            tile.refreshGrace();
            this.addPatch(packet);
        } else {
            if (!this.removeIfEmpty(key.pos())) {
                this.addPatch(packet);
            }
        }

        return true;
    }

    public void tryRemoveData(final BlockPos pos, final Direction dir, final double depth) {
        final GServerBlock block = this.graffiti.get(pos);
        if (block == null) {
            return;
        }

        block.tryRemoveData(dir, depth);
    }

    /**
     * @return true if block data is marked for removal
     */
    public boolean removeIfEmpty(final BlockPos pos) {
        final GServerBlock block = this.graffiti.get(pos);
        if (block != null && block.isEmpty()) {
            this.markForRemoval(pos);
            return true;
        }

        return false;
    }

    public @Nullable GServerDataHolder getOrConditionalCreate(final BlockPos pos, final Direction dir, final double depth, final boolean clear) {
        if (clear) {
            return this.get(pos, dir, depth);
        } else {
            return this.getOrCreate(pos, dir, depth);
        }
    }

    public GServerDataHolder getOrCreate(final BlockPos pos, final Direction dir, final double depth) {
        return this.graffiti.computeIfAbsent(pos, _ -> new GServerBlock(pos)).getOrCreate(dir, depth);
    }

    public @Nullable GServerDataHolder get(final BlockPos pos, final Direction dir, final double depth) {
        final GServerBlock blockData = this.graffiti.get(pos);
        if (blockData == null) {
            return null;
        }

        return blockData.get(dir, depth);
    }

    public enum Type {
        SMOTHERED, REMOVED, FULL_RESEND
    }
}
