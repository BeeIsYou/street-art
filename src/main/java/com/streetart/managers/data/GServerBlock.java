package com.streetart.managers.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GServerBlock {

    public static final Codec<GServerBlock> CODEC = RecordCodecBuilder.create(i -> i.group(
                    BlockDataMapper.CODEC.fieldOf("data_map").forGetter(b -> b.blockData),
                    BlockPos.CODEC.fieldOf("pos").forGetter(GServerBlock::getBlockPos),
                    UUIDUtil.CODEC.optionalFieldOf("recent_player").forGetter(g -> Optional.ofNullable(g.recentPlayerID)))
            .apply(i, GServerBlock::new)
    );

    private final BlockPos blockPos;

    private final BlockDataMapper blockData;

    //TODO: maybe make this more robust, describing where the blame is coming from if not a player (IE dispenser / fill command from command block)
    @Nullable
    private UUID recentPlayerID;

    public GServerBlock(final BlockPos pos) {
        this.blockPos = pos;
        this.blockData = new BlockDataMapper();
    }

    private GServerBlock(final BlockDataMapper data, final BlockPos pos, final Optional<UUID> recentPlayerID) {
        this.blockPos = pos;
        this.blockData = data;
        this.recentPlayerID = recentPlayerID.orElse(null);
    }

    public void handleSmothered(final List<TempData> gatherer, final Direction dir) {
        this.blockData.smotheredFromDir(gatherer, this.blockPos, dir);
    }

    public List<TempData> compileData() {
        return this.blockData.compileData(this.blockPos);
    }

    public boolean randomDecay(final ServerLevel level) {
        return this.blockData.randomDecay(level.getRandom());
    }

    public GServerDataHolder getOrCreate(final Direction dir, final int depth) {
        return this.blockData.getOrCreate(dir, depth);
    }

    public GServerDataHolder get(final Direction dir, final int depth) {
        return this.blockData.getFromDepth(dir, depth);
    }

    public void tryRemoveData(final Direction dir, final int depth) {
        this.blockData.removeDepth(dir, depth);
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    /**
     * Pwetty pwease do not modify the entries inside
     */
    public Collection<Map.Entry<Direction, GServerDataHolder[]>> getImmutableIterator() {
        return this.blockData.getImmutableIterator();
    }

    public boolean isEmpty() {
        return this.blockData.isCompletelyEmpty();
    }

    public void blame(@Nullable final ServerPlayer player) {
        if (player != null) {
            this.recentPlayerID = player.getUUID();
        } else {
            this.recentPlayerID = null;
        }
    }

    public record Snapshot(BlockDataMapper map, UUID recentPlayerID, BlockPos pos) {
        public static Snapshot generate(final GServerBlock block) {
            return new Snapshot(BlockDataMapper.deepCopy(block.blockData), block.recentPlayerID, block.blockPos);
        }
    }
}
