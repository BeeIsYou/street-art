package com.streetart.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.GBlock;
import com.streetart.GManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Map;

public class GServerBlock extends GBlock<GServerDataHolder> {

    public static final Codec<GServerBlock> CODEC = RecordCodecBuilder.create(i -> i.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(GBlock::getBlockPos),
                    Codec.unboundedMap(Direction.CODEC, Codec.list(GServerDataHolder.CODEC)).fieldOf("data_map").forGetter(GBlock::getBlockData))
            .apply(i, (pos, data) -> new GServerBlock(data, pos))
    );

    public GServerBlock(final BlockPos pos) {
        super(pos);
    }

    public GServerBlock(final Map<Direction, List<GServerDataHolder>> blockData, final BlockPos pos) {
        super(blockData, pos);
    }

    @Override
    public GServerDataHolder createData(final Direction dir, final double depth, final BlockPos pos, final GManager<GServerDataHolder, ? extends GBlock<GServerDataHolder>> graffitiManager) {
        return new GServerDataHolder(depth);
    }

    @Override
    public void close() {
    }

}
