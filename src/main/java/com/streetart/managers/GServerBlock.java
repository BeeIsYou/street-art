package com.streetart.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.GBlock;
import com.streetart.GManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Iterator;
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

    /**
     * @return true if no more data exists within the block
     */
    public boolean randomDecay(ServerLevel level) {
        Iterator<Map.Entry<Direction, List<GServerDataHolder>>> it1 = this.getBlockData().entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry<Direction, List<GServerDataHolder>> entry = it1.next();

            entry.getValue().removeIf(gServerDataHolder -> gServerDataHolder.randomDecay(level.getRandom()));

            if (entry.getValue().isEmpty()) {
                it1.remove();
            }
        }

        return this.getBlockData().isEmpty();
    }

    @Override
    public GServerDataHolder createData(final Direction dir, final double depth, final BlockPos pos, final GManager<GServerDataHolder, ? extends GBlock<GServerDataHolder>> graffitiManager) {
        return new GServerDataHolder(depth);
    }

    @Override
    public void close() {
    }

}
