package com.streetart.managers.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    private static final int MAX_SIZE = 16;

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

    public void smotheredFromDir(final List<TempData> gatherer, final BlockPos pos, final Direction dir) {
        final GServerDataHolder[] data = this.blockData.get(dir);

        if (data != null) {
            final GServerDataHolder holder = data[15];
            if (holder != null) {
                gatherer.add(new TempData(holder, pos, dir));
            }
        }
    }

    public boolean randomDecay(final RandomSource source) {
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

                if (holder.randomDecay(source)) {
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

    public List<TempData> compileData(final BlockPos pos) {
        final List<TempData> compiled = new ArrayList<>();

        this.blockData.forEach((dir, holder) -> {
            for (final GServerDataHolder gServerDataHolder : holder) {
                if (gServerDataHolder != null) {
                    compiled.add(new TempData(gServerDataHolder, pos, dir));
                }
            }
        });

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
}
