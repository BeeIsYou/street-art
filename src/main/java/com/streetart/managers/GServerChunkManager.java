package com.streetart.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.AllGameRules;
import com.streetart.AllGraffitiLayers;
import com.streetart.StreetArt;
import com.streetart.arealib.AreaLib;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.GraffitiChangeData;
import com.streetart.graffiti_data.GraffitiKey;
import com.streetart.graffiti_data.GraffitiLayerType;
import com.streetart.managers.data.ExposedGraffitiData;
import com.streetart.managers.data.GServerBlock;
import com.streetart.managers.data.GServerDataHolder;
import com.streetart.networking.BiDirectionalGraffitiChange;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ClientBoundInvalidateBlock;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Contract;
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
    private final EnumMap<Type, List<ExposedGraffitiData>> dirtyDatas;
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
        final List<ExposedGraffitiData> typeDatas = this.dirtyDatas.get(type);
        for (final ExposedGraffitiData exposedGraffitiData : typeDatas) {
            final CustomPacketPayload packet = type.getPacket(exposedGraffitiData);
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
    private boolean sendAllDirty(final ServerLevel level, final ChunkPos pos, final Type type, final Consumer<ExposedGraffitiData> dataConsumer) {
        final List<ExposedGraffitiData> typeDatas = this.dirtyDatas.get(type);
        for (final ExposedGraffitiData exposedGraffitiData : typeDatas) {
            final CustomPacketPayload packet = type.getPacket(exposedGraffitiData);
            for (final ServerPlayer player : PlayerLookup.tracking(level, pos)) {
                ServerPlayNetworking.send(player, packet);
            }
            dataConsumer.accept(exposedGraffitiData);
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

        shouldSaveData |= this.sendAllDirty(level, pos, Type.SMOTHERED, this::tryRemoveData);
        shouldSaveData |= this.sendAllDirty(level, pos, Type.REMOVED,
                tempData -> this.graffiti.remove(tempData.pos())
        );

        return shouldSaveData;
    }

    //TODO: REMOVE REMOVE REMOVE REMOVE
    public Iterable<BlockPos> allGraffitiBlocksPositions() {
        return this.graffiti.keySet();
    }

    public void markFullResend(final Identifier layer, final GServerDataHolder data, final BlockPos pos, final Direction dir) {
        this.dirtyDatas.get(Type.FULL_RESEND).add(new ExposedGraffitiData(layer, data, pos, dir));
    }

    /**
     * @return true if a block existed at that position
     */
    public boolean markForRemoval(final BlockPos pos) {
        if (this.graffiti.containsKey(pos)) {
            //TODO: just send pos data to client instead of graffiti data
            this.dirtyDatas.get(Type.REMOVED).add(new ExposedGraffitiData(null, null, pos, null));
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
                            final List<ExposedGraffitiData> exposed = block.compileData(AllGraffitiLayers.DEFAULT_LAYER.identifier());
                            if (exposed != null) {
                                this.dirtyDatas.get(Type.FULL_RESEND).addAll(exposed);
                            }
                        }
                    }
                }
            }
        }
    }

    public void handleRequest(final ServerPlayNetworking.Context context) {
        for (final GServerBlock value : this.graffiti.values()) {
            for (GraffitiLayerType graffitiLayerType : AllGraffitiLayers.LAYER_REGISTRY) {
                // todo conditional syncing if player cannot see layer?
                final List<ExposedGraffitiData> exposedGraffitiData = value.compileData(graffitiLayerType.identifier());
                if (exposedGraffitiData != null) {
                    for (final ExposedGraffitiData compileDatum : exposedGraffitiData) {
                        ServerPlayNetworking.send(context.player(), new ClientBoundGraffitiSet(
                                Optional.of(graffitiLayerType.identifier()),
                                value.getBlockPos(),
                                compileDatum.dir(),
                                compileDatum.data().depth,
                                compileDatum.data().getGraffitiData().array()
                        ));
                    }
                }
            }
        }
    }

    public boolean handleChange(final ServerPlayer player, final BiDirectionalGraffitiChange packet, final GraffitiKey key, final GraffitiChangeData change) {
        final int depth = Mth.clamp(key.depth(), 0, 15);
        final GServerDataHolder tile = this.getOrConditionalCreateFace(
                packet.layer(),
                key.pos(),
                key.dir(),
                depth,
                packet.content() == ColorComponent.CLEAR.id
        );

        if (tile == null) {
            return false;
        }

        if (tile.handleChange(packet.content(), change)) {
            this.tryRemoveData(packet.layer(), key);
        }

        if (packet.content() != 0) {
            tile.refreshGrace();
            this.addPatch(packet);
        } else {
            if (!this.removeIfEmpty(key.pos())) {
                this.addPatch(packet);
            }
        }

        this.blame(player, key.pos());

        return true;
    }

    public void tryRemoveData(final Identifier layer, final GraffitiKey key) {
        final GServerBlock block = this.graffiti.get(key.pos());
        if (block == null) {
            return;
        }

        block.tryRemoveData(layer, key.dir(), key.depth());
    }

    public void tryRemoveData(final ExposedGraffitiData data) {
        final GServerBlock block = this.graffiti.get(data.pos());
        if (block == null) {
            return;
        }

        assert data.data() != null;
        block.tryRemoveData(data.layer(), data.dir(), data.data().depth);
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

    @Contract(pure = true)
    public @Nullable GServerBlock getBlock(final BlockPos pos) {
        return this.graffiti.get(pos);
    }

    @Nullable
    public GServerDataHolder getOrConditionalCreateFace(final Identifier layer, final BlockPos pos, final Direction dir, final int depth, final boolean clear) {
        //make sure we don't create new data if we're just clearing paint
        if (clear) {
            final GServerBlock block = this.graffiti.get(pos);
            if (block != null) {
                return block.get(layer, dir, depth);
            }
        } else {
            return this.graffiti.computeIfAbsent(pos, _ -> new GServerBlock(pos))
                    .getOrCreate(layer, dir, depth);
        }

        return null;
    }

    public void blame(@Nullable final Entity entity, final BlockPos pos) {
        if (entity instanceof ServerPlayer || entity == null) {
            final GServerBlock gblock = this.graffiti.get(pos);
            if (gblock != null) {
                gblock.blame((ServerPlayer) entity);
            }
        }
    }

    public enum Type {
        FULL_RESEND(ClientBoundGraffitiSet::getSetPacket),
        SMOTHERED(ClientBoundGraffitiSet::getSmotheredPacket),
        REMOVED(ClientBoundInvalidateBlock::getPacket);
        private final Function<ExposedGraffitiData, CustomPacketPayload> packetConstructor;

        Type(final Function<ExposedGraffitiData, CustomPacketPayload> packetConstructor) {
            this.packetConstructor = packetConstructor;
        }

        public CustomPacketPayload getPacket(final ExposedGraffitiData exposedGraffitiData) {
            return this.packetConstructor.apply(exposedGraffitiData);
        }
    }
}
