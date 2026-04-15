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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class GServerChunkManager {

    public static final Codec<GServerChunkManager> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.list(GServerBlock.CODEC).fieldOf("block_data").forGetter(manager -> manager.graffiti.values().stream().toList()))
            .apply(i, GServerChunkManager::new)
    );

    private final Map<BlockPos, GServerBlock> graffiti;
    private final EnumMap<Type, List<TempData>> dirtyDatas;
    private final List<BiDirectionalGraffitiChange> patches = new ArrayList<>();

    public GServerChunkManager() {
        this.graffiti = new HashMap<>();
        this.dirtyDatas = new EnumMap<>(Type.class);
        for (final Type value : Type.values()) {
            this.dirtyDatas.put(value, new ArrayList<>());
        }
    }

    public GServerChunkManager(final List<GServerBlock> gServerBlocks) {
        this();
        for (final GServerBlock b : gServerBlocks) {
            this.graffiti.put(b.getBlockPos(), b);
        }
    }

    /**
     * @return true if anything was sent
     */
    private boolean sendAllDirty(final ServerLevel level, final ChunkPos pos, final Type type) {
        final List<TempData> typeDatas = this.dirtyDatas.get(type);
        for (final TempData tempData : typeDatas) {
            final CustomPacketPayload packet = type.getPacket(tempData);
            for (final ServerPlayer player : PlayerLookup.tracking(level, pos)) {
                ServerPlayNetworking.send(player, packet);
            }
        }
        final boolean sent = !typeDatas.isEmpty();
        typeDatas.clear();
        return sent;
    }

    /**
     * @param dataConsumer method to call for each data in the list
     * @return true if anything was set
     */
    private boolean sendAllDirty(final ServerLevel level, final ChunkPos pos, final Type type, final Consumer<TempData> dataConsumer) {
        final List<TempData> typeDatas = this.dirtyDatas.get(type);
        for (final TempData tempData : typeDatas) {
            final CustomPacketPayload packet = type.getPacket(tempData);
            for (final ServerPlayer player : PlayerLookup.tracking(level, pos)) {
                ServerPlayNetworking.send(player, packet);
            }
            dataConsumer.accept(tempData);
        }
        final boolean sent = !typeDatas.isEmpty();
        typeDatas.clear();
        return sent;
    }

    public boolean tick(final ServerLevel level, final ChunkPos pos) {
        this.tickDecay(level, pos);

        boolean shouldSaveData = false;

        shouldSaveData |= this.sendAllDirty(level, pos, Type.FULL_RESEND);

        shouldSaveData |= !this.patches.isEmpty();
        this.patches.removeIf(patch -> {
            for (final ServerPlayer player : PlayerLookup.tracking(level, pos)) {
                ServerPlayNetworking.send(player, patch);
            }
            return true;
        });

        shouldSaveData |= this.sendAllDirty(level, pos, Type.SMOTHERED);
        shouldSaveData |= this.sendAllDirty(level, pos, Type.REMOVED,
                tempData -> this.graffiti.remove(tempData.pos())
        );

        return shouldSaveData;
    }

    public <T extends GServerBlock & PublicFacingBlockData> Iterable<T> allGraffitiBlockData() {
        return (Iterable<T>) this.graffiti.values();
    }

    public Iterable<BlockPos> allGraffitiBlocksPositions() {
        return this.graffiti.keySet();
    }

    public void markFullResend(final GServerDataHolder data, final BlockPos pos, final Direction dir) {
        this.dirtyDatas.get(Type.FULL_RESEND).add(new TempData(data, pos, dir));
    }

    /**
     * @return true if a block existed at that position
     */
    public boolean markForRemoval(final BlockPos pos) {
        if (this.graffiti.containsKey(pos)) {
            this.dirtyDatas.get(Type.REMOVED).add(new TempData(null, pos, null));
            return true;
        }
        return false;
    }

    public void markSmothered(final BlockPos pos, final Direction dir) {
        final GServerBlock block = this.graffiti.get(pos);
        if (block != null) {
            block.handleSmothered(this.dirtyDatas.get(Type.SMOTHERED), dir);
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
                            this.dirtyDatas.get(Type.FULL_RESEND).addAll(block.compileData());
                        }
                    }
                }
            }
        }
    }

    public void handleRequest(final ServerPlayNetworking.Context context) {
        for (final GServerBlock value : this.graffiti.values()) {
            for (final TempData compileDatum : value.compileData()) {
                ServerPlayNetworking.send(context.player(), new ClientBoundGraffitiSet(
                        value.getBlockPos(),
                        compileDatum.dir(),
                        compileDatum.data().getDepth(),
                        compileDatum.data().getGraffitiData().array()
                ));
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
        FULL_RESEND(ClientBoundGraffitiSet::getSetPacket),
        SMOTHERED(ClientBoundGraffitiSet::getSmotheredPacket),
        REMOVED(ClientBoundInvalidateBlock::getPacket);
        private final Function<TempData, CustomPacketPayload> packetConstructor;

        Type(final Function<TempData, CustomPacketPayload> packetConstructor) {
            this.packetConstructor = packetConstructor;
        }

        public CustomPacketPayload getPacket(final TempData tempData) {
            return this.packetConstructor.apply(tempData);
        }
    }
}
