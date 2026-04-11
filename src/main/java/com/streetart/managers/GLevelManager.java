package com.streetart.managers;

import com.streetart.GManager;
import com.streetart.networking.ClientBoundGraffitiUpdate;
import com.streetart.networking.ClientBoundInvalidateBlock;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GLevelManager extends GManager<GServerData, GServerBlock> {
    private final Map<BlockPos, GServerBlock> graffiti = new HashMap<>();
    private final List<TempData> dirtyData = new ArrayList<>();
    private final List<BlockPos> toRemove = new ArrayList<>();
    private final List<TempData> smothered = new ArrayList<>();

    private final ServerLevel level;

    @Override
    public Map<BlockPos, GServerBlock> getGraffiti() {
        return this.graffiti;
    }

    public GLevelManager(final ServerLevel level) {
        this.level = level;
    }

    public void markDirty(GServerData data, final BlockPos pos, final Direction dir) {
        this.dirtyData.add(new TempData(data, pos, dir));
        data.dirty = true;
    }

    public void markForRemoval(BlockPos pos) {
        if (this.getGraffiti().containsKey(pos)) {
            this.toRemove.add(pos);
        }
    }

    public void markSmothered(BlockPos pos, Direction dir) {
        GServerBlock block = this.getGraffiti().get(pos);
        if (block != null) {
            List<GServerData> dataList = block.getBlockData().get(dir);
            if (dataList != null) {
                for (GServerData data : dataList) {
                    if (data.depth == 1) {
                        this.smothered.add(new TempData(data, pos, dir));
                    }
                }
            }
        }
    }

    public void tick() {
        this.dirtyData.removeIf(dataHolder -> {
            if (dataHolder.data.dirty) {
                dataHolder.data.dirty = false;
                for (final ServerPlayer player : PlayerLookup.around(this.level, dataHolder.pos.getCenter(), 100)) {
                    ServerPlayNetworking.send(player, new ClientBoundGraffitiUpdate(
                            dataHolder.pos,
                            dataHolder.dir,
                            dataHolder.data.depth,
                            dataHolder.data.graffitiData
                    ));
                }

            }

            return true;
        });

        this.toRemove.removeIf(pos -> {
            for (final ServerPlayer player : PlayerLookup.around(this.level, pos.getCenter(), 100)) {
                ServerPlayNetworking.send(player, new ClientBoundInvalidateBlock(pos));
            }
            this.getGraffiti().remove(pos);
            return true;
        });

        this.smothered.removeIf(dataHolder -> {
            GServerBlock block = this.getGraffiti().get(dataHolder.pos);
            List<GServerData> dataList = block.getBlockData().get(dataHolder.dir);
            if (dataList != null) {
                dataList.remove(dataHolder.data);
                for (final ServerPlayer player : PlayerLookup.around(this.level, dataHolder.pos.getCenter(), 100)) {
                    ServerPlayNetworking.send(player, new ClientBoundGraffitiUpdate(
                            dataHolder.pos,
                            dataHolder.dir,
                            dataHolder.data.depth,
                            new byte[0]
                    ));
                }
            }
            return true;
        });
    }

    @Override
    public GServerBlock newBlockData(BlockPos pos) {
        return new GServerBlock(pos);
    }

    @Override
    public void close() {}

    public record TempData(GServerData data, BlockPos pos, Direction dir) { }
}
