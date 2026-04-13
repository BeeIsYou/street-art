package com.streetart.graffiti_data;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TileKey(BlockPos pos, Direction dir, double depth) {
    public static final StreamCodec<ByteBuf, TileKey> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TileKey::pos,
            Direction.STREAM_CODEC, TileKey::dir,
            ByteBufCodecs.DOUBLE, TileKey::depth,
            TileKey::new
    );

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;

        TileKey that = (TileKey) o;
        return Double.compare(this.depth, that.depth) == 0 && this.pos.equals(that.pos) && this.dir == that.dir;
    }

    @Override
    public int hashCode() {
        int result = this.pos.hashCode();
        result = 31 * result + this.dir.hashCode();
        result = 31 * result + Double.hashCode(this.depth);
        return result;
    }
}
