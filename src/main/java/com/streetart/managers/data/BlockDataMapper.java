package com.streetart.managers.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.*;

public class BlockDataMapper {

    public static final Codec<BlockDataMapper> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Direction.CODEC, Codec.list(GServerDataHolder.CODEC)).fieldOf("data").forGetter(d -> d.blockData))
            .apply(i, BlockDataMapper::new)
    );

    private final EnumMap<Direction, List<GServerDataHolder>> blockData;
    
    public BlockDataMapper() {
        this.blockData = new EnumMap<>(Direction.class);
    }

    public BlockDataMapper(final Map<Direction, List<GServerDataHolder>> blockData) {
        final EnumMap<Direction, List<GServerDataHolder>> map = new EnumMap<>(Direction.class);

        //grrr mc grrrrrr
        //we need to iterate over all entries as we need to be able to mutate the inner lists here
        for (final Map.Entry<Direction, List<GServerDataHolder>> entries : blockData.entrySet()) {
            map.put(entries.getKey(), new ArrayList<>(entries.getValue()));
        }

        this.blockData = map;
    }

    public void smotheredFromDir(final List<TempData> gatherer, BlockPos pos, final Direction dir) {
        final List<GServerDataHolder> data = this.blockData.get(dir);
        if (data != null) {
            for (final GServerDataHolder holder : data) {
                if (holder.getDepth() == 1) {
                    gatherer.add(new TempData(holder, pos, dir));
                }
            }
        }
    }

    public boolean randomDecay(final RandomSource source) {
        this.blockData.entrySet().removeIf(eSet -> {
            eSet.getValue().removeIf(holder -> holder.randomDecay(source));
            return eSet.getValue().isEmpty();
        });

        return this.isCompletelyEmpty();
    }

    public GServerDataHolder getOrCreate(final Direction dir, final double depth) {
        final List<GServerDataHolder> dataList = this.blockData.computeIfAbsent(dir, _ -> new ArrayList<>(6));

        final double snapped = GServerBlock.snapToGrid(depth);
        for (final GServerDataHolder holder : dataList) {
            if (holder.getDepth() == snapped) {
                return holder;
            }
        }

        final GServerDataHolder holder = new GServerDataHolder(snapped);
        dataList.add(holder);
        return holder;
    }

    public GServerDataHolder getFromDepth(final Direction dir, final double depth) {
        final List<GServerDataHolder> holders = this.blockData.get(dir);
        if (holders == null) {
            return null;
        }

        final double snapped = GServerBlock.snapToGrid(depth);
        for (final GServerDataHolder holder : holders) {
            if (holder.getDepth() == snapped) {
                return holder;
            }
        }

        return null;
    }

    public void removeDepth(final Direction dir, final double depth) {
        final List<GServerDataHolder> holders = this.blockData.get(dir);
        if (holders != null) {
            if (!holders.isEmpty()) {
                final double snapped = GServerBlock.snapToGrid(depth);

                final Iterator<GServerDataHolder> iter = holders.iterator();
                while (iter.hasNext()) {
                    final GServerDataHolder data = iter.next();

                    if (data.getDepth() == snapped) {
                        iter.remove();
                        break;
                    }
                }
            }

            if (holders.isEmpty()) {
                this.blockData.remove(dir);
            }
        }
    }

    public boolean isCompletelyEmpty() {
        return this.blockData.isEmpty();
    }
}
