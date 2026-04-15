package com.streetart.managers.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.managers.public_facing_interfaces.PublicFacingBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class GServerBlock implements PublicFacingBlockData {

    public static final Codec<GServerBlock> CODEC = RecordCodecBuilder.create(i -> i.group(
                    BlockDataMapper.CODEC.fieldOf("data_map").forGetter(b -> b.blockData),
                    BlockPos.CODEC.fieldOf("pos").forGetter(GServerBlock::getBlockPos))
            .apply(i, GServerBlock::new)
    );

    private final BlockPos blockPos;
    private final BlockDataMapper blockData;

    public GServerBlock(final BlockPos pos) {
        this.blockPos = pos;
        this.blockData = new BlockDataMapper();
    }

    private GServerBlock(final BlockDataMapper data, final BlockPos pos) {
        this.blockPos = pos;
        this.blockData = data;
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

    public GServerDataHolder getOrCreate(final Direction dir, final double depth) {
        return this.blockData.getOrCreate(dir, depth);
    }

    public GServerDataHolder get(final Direction dir, final double depth) {
        return this.blockData.getFromDepth(dir, depth);
    }

    public void tryRemoveData(final Direction dir, final double depth) {
        this.blockData.removeDepth(dir, depth);
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public boolean isEmpty() {
        return this.blockData.isCompletelyEmpty();
    }

    public static double snapToGrid(final double v) {
        return Math.round(v * 16) / 16d;
    }
}
