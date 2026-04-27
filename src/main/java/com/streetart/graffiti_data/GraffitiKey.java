package com.streetart.graffiti_data;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GraffitiKey(BlockPos pos, Direction dir, int depth) {
    public static final StreamCodec<ByteBuf, GraffitiKey> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, GraffitiKey::pos,
            Direction.STREAM_CODEC, GraffitiKey::dir,
            ByteBufCodecs.INT, GraffitiKey::depth,
            GraffitiKey::new
    );

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;

        GraffitiKey that = (GraffitiKey) o;
        return Double.compare(this.depth, that.depth) == 0 && this.pos.equals(that.pos) && this.dir == that.dir;
    }

    @Override
    public int hashCode() {
        int result = this.pos.hashCode();
        result = 31 * result + this.dir.hashCode();
        result = 31 * result + this.depth;
        return result;
    }
}
