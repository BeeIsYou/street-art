package com.streetart.managers.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.graffiti_data.GraffitiChangeData;
import com.streetart.graffiti_data.GraffitiKey;
import com.streetart.networking.BiDirectionalGraffitiChange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockDataMapper {

    public static final Codec<BlockDataMapper> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.list(GServerDataHolder.CODEC).fieldOf("data").forGetter(d -> {
                        ArrayList<GServerDataHolder> holders = new ArrayList<>();
                        for (Map.Entry<Direction, GServerDataHolder[]> eset : d.blockData.entrySet()) {
                            for (GServerDataHolder holder : eset.getValue()) {
                                if (holder != null) {
                                    holders.add(holder);
                                }
                            }
                        }

                        return holders;
                    }))
            .apply(i, BlockDataMapper::new)
    );

    private final EnumMap<Direction, GServerDataHolder[]> blockData;
    public static final int MAX_SIZE = 16;

    public BlockDataMapper() {
        this.blockData = new EnumMap<>(Direction.class);
    }

    public BlockDataMapper(final List<GServerDataHolder> holders) {
        final EnumMap<Direction, GServerDataHolder[]> map = new EnumMap<>(Direction.class);

        for (final GServerDataHolder holder : holders) {
            map.computeIfAbsent(holder.dir, _ ->
                    new GServerDataHolder[MAX_SIZE])[holder.depth] = holder;
        }

        this.blockData = map;
    }

    public BlockDataMapper(final EnumMap<Direction, GServerDataHolder[]> blockData) {
        final EnumMap<Direction, GServerDataHolder[]> map = new EnumMap<>(Direction.class);
        map.putAll(blockData);
        this.blockData = map;
    }

    public GServerDataHolder smotheredFromDir(final Direction dir) {
        final GServerDataHolder[] data = this.blockData.get(dir);
        if (data != null) {
            return data[0];
        }

        return null;
    }

    public boolean randomDecay(final RandomSource source, final BlockPos pos, final BiDirectionalGraffitiChange change) {
        this.blockData.entrySet().removeIf(eSet -> {
            boolean passWithoutFullClear = false; //only true when a decay happens but doesn't remove all data
            boolean dataAvailable = false; //only true when there is still data left inside array

            final int beforeLength = eSet.getValue().length;
            for (int i = 0; i < beforeLength; i++) {
                final GServerDataHolder holder = eSet.getValue()[i];
                if (holder != null) {
                    dataAvailable = true;
                } else {
                    continue;
                }

                final GraffitiChangeData data = change.changes().computeIfAbsent(
                        new GraffitiKey(pos, holder.dir, holder.depth), _ -> GraffitiChangeData.empty());
                if (holder.randomDecay(source, data)) {
                    if (!passWithoutFullClear) {
                        dataAvailable = false;
                    }

                    eSet.getValue()[i] = null;
                } else {
                    passWithoutFullClear = true;
                }
            }

            return !dataAvailable;
        });

        return this.isCompletelyEmpty();
    }

    public GServerDataHolder getOrCreate(final Direction dir, final int depth) {
        final GServerDataHolder[] dataList = this.blockData.computeIfAbsent(dir, _ -> new GServerDataHolder[MAX_SIZE]);

        GServerDataHolder holder = dataList[depth];
        if (holder != null) {
            return holder;
        }

        holder = new GServerDataHolder(depth, dir);
        dataList[depth] = holder;
        return holder;
    }

    @Nullable
    public GServerDataHolder getFromDepth(final Direction dir, final int depth) {
        final GServerDataHolder[] holders = this.blockData.get(dir);
        if (holders != null) {
            return holders[depth];
        }

        return null;
    }

    public void removeDepth(final Direction dir, final int depth) {
        final GServerDataHolder[] holders = this.blockData.get(dir);
        holders[depth] = null;

        boolean isEmpty = true;
        for (final GServerDataHolder holder : holders) {
            if (holder != null) {
                isEmpty = false;
                break;
            }
        }

        if (isEmpty) {
            this.blockData.remove(dir);
        }
    }

    public List<GServerDataHolder> compileData() {
        final List<GServerDataHolder> compiled = new ArrayList<>();

        for (final GServerDataHolder[] value : this.blockData.values()) {
            for (final GServerDataHolder holder : value) {
                if (holder != null) {
                    compiled.add(holder);
                }
            }
        }

        return compiled;
    }

    public boolean isCompletelyEmpty() {
        return this.blockData.isEmpty();
    }

    public static BlockDataMapper deepCopy(final BlockDataMapper toCopy) {
        return new BlockDataMapper(toCopy.blockData);
    }

    /**
     * Pwetty pwease do not modify the entries inside
     */
    public Collection<Map.Entry<Direction, GServerDataHolder[]>> getImmutableIterator() {
        return Collections.unmodifiableCollection(this.blockData.entrySet());
    }

    /**
     * Provides the relative index from the given depth. depth is assumed to already be normalized by 1/16th.
     *
     * @param depth The depth to calculate the index from.
     * @return The index of the given depth
     */
    private int fromDepthToIndex(final double depth) {
        return (int) (16 * depth) - 1;
    }

    public BlockDataMapper copy() {
        final BlockDataMapper newData = new BlockDataMapper();
        for (final Map.Entry<Direction, GServerDataHolder[]> entry : this.blockData.entrySet()) {
            final GServerDataHolder[] copiedData = new GServerDataHolder[MAX_SIZE];
            for (int i = 0; i < MAX_SIZE; i++) {
                if (entry.getValue()[i] != null) {
                    copiedData[i] = entry.getValue()[i].copy();
                }
            }
            newData.blockData.put(entry.getKey(), copiedData);
        }

        return newData;
    }
}
