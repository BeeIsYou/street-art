package com.streetart.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.GManager;
import com.streetart.StreetArt;
import com.streetart.networking.BiDirectionalGraffitiChange;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ClientBoundInvalidateBlock;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GServerChunkManager extends GManager<GServerDataHolder, GServerBlock> {

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

    @Override
    public Map<BlockPos, GServerBlock> getGraffiti() {
        return this.graffiti;
    }

    public void markDirty(final GServerDataHolder data, final BlockPos pos, final Direction dir) {
        this.dirtyData.add(new TempData(data, pos, dir, Type.FULL_RESEND));
    }

    public void markForRemoval(final BlockPos pos) {
        if (this.getGraffiti().containsKey(pos)) {
            this.dirtyData.add(new TempData(null, pos, null, Type.REMOVED));
        }
    }

    public void markSmothered(final BlockPos pos, final Direction dir) {
        final GServerBlock block = this.getGraffiti().get(pos);
        if (block != null) {
            final List<GServerDataHolder> dataList = block.getBlockData().get(dir);
            if (dataList != null) {
                for (final GServerDataHolder data : dataList) {
                    if (data.getDepth() == 1) {
                        this.dirtyData.add(new TempData(data, pos, dir, Type.SMOTHERED));
                    }
                }
            }
        }
    }

    public void addPatch(final BiDirectionalGraffitiChange patch) {
        this.patches.add(patch);
    }

    public void tickDecay(final ServerLevel level, final ChunkPos pos) {
        for (int i = 0; i < level.getSectionsCount(); i++) {
            for (int j = 0; j < 3; j++) {
                final BlockPos randomPos = level.getBlockRandomPos(pos.getMinBlockX(), level.getSectionYFromSectionIndex(i), pos.getMinBlockZ(), 15);
                if (StreetArt.AREA_LIB.decays(level, randomPos)) {
                    final GServerBlock block = this.getGraffiti().get(randomPos);
                    if (block != null) {
                        if (block.randomDecay(level)) {
                            this.markForRemoval(randomPos);
                        } else {
                            block.getBlockData().forEach((dir, datas) -> {
                                for (final GServerDataHolder data : datas) {
                                    this.markDirty(data, randomPos, dir);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    public boolean tick(final ServerLevel level, final ChunkPos pos) {
        this.tickDecay(level, pos);

        final boolean shouldSaveData = !this.dirtyData.isEmpty();

        this.dirtyData.removeIf(tempData -> {
            final Type type = tempData.type;

            CustomPacketPayload packet = null;
            switch (type) {
                case FULL_RESEND -> {
                    packet = new ClientBoundGraffitiSet(
                            tempData.pos,
                            tempData.dir,
                            tempData.data.getDepth(),
                            tempData.data.getGraffitiData().array());
                }

                case REMOVED -> {
                    this.getGraffiti().remove(tempData.pos);
                    packet = new ClientBoundInvalidateBlock(tempData.pos);
                }

                case SMOTHERED ->{
                    final GServerBlock block = this.getGraffiti().get(tempData.pos);
                    final List<GServerDataHolder> dataList = block.getBlockData().get(tempData.dir);
                    dataList.remove(tempData.data);

                    packet = new ClientBoundGraffitiSet(
                            tempData.pos,
                            tempData.dir,
                            tempData.data.getDepth(),
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

    @Override
    public GServerBlock newBlockData(final BlockPos pos) {
        return new GServerBlock(pos);
    }

    @Override
    public void close() {
    }

    //data, dir nullable -> type == REMOVED
    public record TempData(GServerDataHolder data, BlockPos pos, Direction dir, Type type) {
    }

    public enum Type {
        SMOTHERED, REMOVED, FULL_RESEND
    }
}
