package com.streetart.managers.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.managers.public_facing_interfaces.PublicFacingBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GServerBlock implements PublicFacingBlockData {

    public static final Codec<GServerBlock> CODEC = RecordCodecBuilder.create(i -> i.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(GServerBlock::getBlockPos),
                    Codec.unboundedMap(Direction.CODEC, Codec.list(GServerDataHolder.CODEC)).fieldOf("data_map").forGetter(b -> b.blockData))
            .apply(i, (pos, data) -> new GServerBlock(data, pos))
    );

    private final BlockPos blockPos;
    private final Map<Direction, List<GServerDataHolder>> blockData;

    public GServerBlock(final BlockPos pos) {
        this(new HashMap<>(), pos);
    }

    public GServerBlock(final Map<Direction, List<GServerDataHolder>> blockData, final BlockPos pos) {
        this.blockPos = pos;

        final HashMap<Direction, List<GServerDataHolder>> map = new HashMap<>();
        //grrr mc grrrrrr
        //we need to iterate over all entries as we need to be able to mutate the inner lists here
        for (final Map.Entry<Direction, List<GServerDataHolder>> entries : blockData.entrySet()) {
            map.put(entries.getKey(), new ArrayList<>(entries.getValue()));
        }

        this.blockData = map;
    }

    @Nullable
    public Iterable<GServerDataHolder> dataFromDir(final Direction dir) {
        return this.blockData.get(dir);
    }

    public Iterable<Direction> allValidDirections() {
        return this.blockData.keySet();
    }

    public void removeHolder(final Direction dir, final GServerDataHolder holder) {
        final List<GServerDataHolder> holders = this.blockData.get(dir);
        if (holders != null) {
            holders.remove(holder);
        }
    }

    public void tryRemoveData(final Direction dir, final double depth) {
        this.remove(dir, depth);
        if (this.blockData.get(dir).isEmpty()) {
            this.blockData.remove(dir);
        }
    }

    public void handleSmothered(final List<TempData> gatherer, final Direction dir) {
        final List<GServerDataHolder> data = this.blockData.get(dir);
        if (data != null) {
            for (final GServerDataHolder holder : data) {
                if (holder.getDepth() == 1) {
                    gatherer.add(new TempData(holder, this.blockPos, dir));
                }
            }
        }
    }

    public GServerDataHolder get(final Direction dir, final double depth) {
        final List<GServerDataHolder> dataList = this.blockData.get(dir);
        if (dataList == null) {
            return null;
        }

        final double snap = snapToGrid(depth);
        for (final GServerDataHolder data : dataList) {
            if (data.getDepth() == snap) {
                return data;
            }
        }

        return null;
    }

    public GServerDataHolder getOrCreate(final Direction dir, final double depth) {
        final List<GServerDataHolder> dataList = this.blockData.computeIfAbsent(dir, _ -> new ArrayList<>(6));
        final double snap = snapToGrid(depth);
        for (final GServerDataHolder data : dataList) {
            if (data.getDepth() == snap) {
                return data;
            }
        }

        final GServerDataHolder created = this.createData(snap);
        dataList.add(created);
        return created;
    }

    public void remove(final Direction dir, final double depth) {
        final List<GServerDataHolder> dataList = this.blockData.get(dir);
        if (dataList == null) {
            return;
        }

        final double snap = snapToGrid(depth);
        dataList.removeIf(data -> data.getDepth() == snap);
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    /**
     * !! will return false if holding empty lists or fully clear textures
     */
    public boolean isEmpty() {
        return this.blockData.isEmpty();
    }

    /**
     * @return true if no more data exists within the block
     */
    public boolean randomDecay(final ServerLevel level) {
        final Iterator<Map.Entry<Direction, List<GServerDataHolder>>> it1 = this.blockData.entrySet().iterator();
        while (it1.hasNext()) {
            final Map.Entry<Direction, List<GServerDataHolder>> entry = it1.next();

            entry.getValue().removeIf(gServerDataHolder -> gServerDataHolder.randomDecay(level.getRandom()));

            if (entry.getValue().isEmpty()) {
                it1.remove();
            }
        }

        return this.isEmpty();
    }

    public GServerDataHolder createData(final double depth) {
        return new GServerDataHolder(depth);
    }

    public static double snapToGrid(final double v) {
        return Math.round(v * 16) / 16d;
    }
}
