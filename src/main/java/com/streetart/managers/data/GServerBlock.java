package com.streetart.managers.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GServerBlock {

    public static final Codec<GServerBlock> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.unboundedMap(Identifier.CODEC, BlockDataMapper.CODEC).fieldOf("layer_map").forGetter(block -> block.layerMap),
                    BlockPos.CODEC.fieldOf("pos").forGetter(GServerBlock::getBlockPos),
                    UUIDUtil.CODEC.optionalFieldOf("recent_player").forGetter(g -> Optional.ofNullable(g.recentPlayerID)))
            .apply(i, GServerBlock::new)
    );

    private final BlockPos blockPos;

    private final Map<Identifier, BlockDataMapper> layerMap;

    //TODO: maybe make this more robust, describing where the blame is coming from if not a player (IE dispenser / fill command from command block)
    @Nullable
    private UUID recentPlayerID;

    public GServerBlock(final BlockPos pos) {
        this.blockPos = pos;
        this.layerMap = new HashMap<>();
    }

    public void copyFrom(final GServerBlock other) {
        this.layerMap.clear();
        for (Map.Entry<Identifier, BlockDataMapper> entries : other.layerMap.entrySet()) {
            this.layerMap.put(entries.getKey(), entries.getValue().copy());
        }
    }

    private GServerBlock(final Map<Identifier, BlockDataMapper> layerMap, final BlockPos pos, final Optional<UUID> recentPlayerID) {
        this.blockPos = pos;

        //work around due to mc giving us an unmodifiable map by defualt
        //TODO: handle this in codec
        this.layerMap = new HashMap<>();
        this.layerMap.putAll(layerMap);

        this.recentPlayerID = recentPlayerID.orElse(null);
    }

    //TODO: see if we want to smother all layers or only the default?
    public void handleSmothered(final List<ExposedGraffitiData> gatherer, final Direction dir) {
        this.layerMap.forEach((layer, mapper) -> {
            final GServerDataHolder holder = mapper.smotheredFromDir(dir);
            if (holder != null) {
                gatherer.add(new ExposedGraffitiData(layer, holder, this.blockPos, dir));
            }
        });
    }

    public List<ExposedGraffitiData> compileData(final Identifier layer) {
        final List<ExposedGraffitiData> list = new ArrayList<>();

        final BlockDataMapper mapper = this.layerMap.get(layer);
        if (mapper != null) {
            for (final GServerDataHolder holder : mapper.compileData()) {
                list.add(new ExposedGraffitiData(layer, holder, this.blockPos, holder.dir));
            }
        }

        return list;
    }

    public boolean randomDecay(final ServerLevel level) {
        //TODO: do we want to only allow decay for default layer? for now all layers are affected.

        final RandomSource random = level.getRandom();
        this.layerMap.entrySet().removeIf(entry -> entry.getValue().randomDecay(random));

        return this.layerMap.isEmpty();
    }

    @Nullable
    public GServerDataHolder get(final Identifier layer, final Direction dir, final int depth) {
        final BlockDataMapper mapper = this.layerMap.get(layer);
        if (mapper != null) {
            return mapper.getFromDepth(dir, depth);
        }

        return null;
    }

    @NotNull
    public GServerDataHolder getOrCreate(final Identifier layer, final Direction dir, final int depth) {
        return this.layerMap.computeIfAbsent(layer, _ -> new BlockDataMapper()).getOrCreate(dir, depth);
    }

    public void tryRemoveData(final Identifier layer, final Direction dir, final int depth) {
        final BlockDataMapper mapper = this.layerMap.get(layer);
        if (mapper != null) {
            mapper.removeDepth(dir, depth);
            if (mapper.isCompletelyEmpty()) {
                this.layerMap.remove(layer);
            }
        }
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    /**
     * Pwetty pwease do not modify the entries inside
     */
    @Nullable
    public Collection<Map.Entry<Direction, GServerDataHolder[]>> getImmutableIterator(final Identifier layer) {
        final BlockDataMapper mapper = this.layerMap.get(layer);
        if (mapper != null) {
            return mapper.getImmutableIterator();
        }

        return null;
    }

    public boolean isEmpty() {
        return this.layerMap.isEmpty();
    }

    public void blame(@Nullable final ServerPlayer player) {
        if (player != null) {
            this.recentPlayerID = player.getUUID();
        } else {
            this.recentPlayerID = null;
        }
    }
}
