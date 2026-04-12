package com.streetart.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.GManager;
import com.streetart.networking.ClientBoundGraffitiUpdate;
import com.streetart.networking.ClientBoundInvalidateBlock;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class GServerChunkManager extends GManager<GServerDataHolder, GServerBlock> {

    public static final Codec<GServerChunkManager> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.list(GServerBlock.CODEC).fieldOf("block_data").forGetter(manager -> manager.graffiti.values().stream().toList()))
            .apply(i, GServerChunkManager::new)
    );

    private final Map<BlockPos, GServerBlock> graffiti;

    private final List<TempData> dirtyData = new ArrayList<>();
    private final List<BlockPos> toRemove = new ArrayList<>();
    private final List<TempData> smothered = new ArrayList<>();

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
        this.dirtyData.add(new TempData(data, pos, dir));
        data.dirty = true;
    }

    public void markForRemoval(final BlockPos pos) {
        if (this.getGraffiti().containsKey(pos)) {
            this.toRemove.add(pos);
        }
    }

    public void markSmothered(final BlockPos pos, final Direction dir) {
        final GServerBlock block = this.getGraffiti().get(pos);
        if (block != null) {
            final List<GServerDataHolder> dataList = block.getBlockData().get(dir);
            if (dataList != null) {
                for (final GServerDataHolder data : dataList) {
                    if (data.getDepth() == 1) {
                        this.smothered.add(new TempData(data, pos, dir));
                    }
                }
            }
        }
    }

    public boolean tick(final ServerLevel level) {
        final boolean shouldSaveData = !this.dirtyData.isEmpty() || !this.toRemove.isEmpty() || !this.smothered.isEmpty();

        this.dirtyData.removeIf(dataHolder -> {
            if (dataHolder.data.dirty) {
                dataHolder.data.dirty = false;
                for (final ServerPlayer player : PlayerLookup.around(level, dataHolder.pos.getCenter(), 100)) {
                    ServerPlayNetworking.send(player, new ClientBoundGraffitiUpdate(
                            dataHolder.pos,
                            dataHolder.dir,
                            dataHolder.data.getDepth(),
                            dataHolder.data.getGraffitiData().array()
                    ));
                }

            }

            return true;
        });

        this.toRemove.removeIf(pos -> {
            for (final ServerPlayer player : PlayerLookup.around(level, pos.getCenter(), 100)) {
                ServerPlayNetworking.send(player, new ClientBoundInvalidateBlock(pos));
            }

            this.getGraffiti().remove(pos);
            return true;
        });

        this.smothered.removeIf(dataHolder -> {
            final GServerBlock block = this.getGraffiti().get(dataHolder.pos);
            final List<GServerDataHolder> dataList = block.getBlockData().get(dataHolder.dir);
            if (dataList != null) {
                dataList.remove(dataHolder.data);
                for (final ServerPlayer player : PlayerLookup.around(level, dataHolder.pos.getCenter(), 100)) {
                    ServerPlayNetworking.send(player, new ClientBoundGraffitiUpdate(
                            dataHolder.pos,
                            dataHolder.dir,
                            dataHolder.data.getDepth(),
                            new byte[0]
                    ));
                }
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
    public void close() {}

    public record TempData(GServerDataHolder data, BlockPos pos, Direction dir) { }
}
