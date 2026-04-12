package com.streetart.networking;

import com.streetart.ArtUtil;
import com.streetart.StreetArt;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;

public record BiDirectionalGraffitiChange(int color, HashMap<TileKey, TileChange> changes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BiDirectionalGraffitiChange> TYPE  = new Type<>(StreetArt.id("graffiti_delta"));
    public static final StreamCodec<ByteBuf, BiDirectionalGraffitiChange> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            BiDirectionalGraffitiChange::color,
            ByteBufCodecs.map(HashMap::new, TileKey.CODEC, TileChange.CODEC),
            BiDirectionalGraffitiChange::changes,
            BiDirectionalGraffitiChange::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void markChanged(BlockPos pos, Direction dir, double depth, int x, int y) {
        TileChange change = this.changes.computeIfAbsent(new TileKey(pos, dir, depth), _ -> TileChange.empty());
        change.markChanged(x, y);
    }

    public void markChanged(BlockHitResult hitResult, int x, int y) {
        this.markChanged(hitResult.getBlockPos(), hitResult.getDirection(), ArtUtil.calculateDepth(hitResult), x, y);
    }

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

    /**
     * An individual plane being modified
     * @param modifiedPixels a 1024 bit (32 byte) mask for where to apply
     */
    public record TileChange(byte[] modifiedPixels) {
        public static final StreamCodec<ByteBuf, TileChange> CODEC = StreamCodec.composite(
                ByteBufCodecs.BYTE_ARRAY, TileChange::modifiedPixels,
                TileChange::new
        );

        public static TileChange empty() {
            return new TileChange(new byte[32]);
        }

        public void markChanged(int x, int y) {
            int i = x + y * 16;
            this.modifiedPixels[i >> 3] |= (byte) (1 << (i & 7));
        }
    }
}
