package com.streetart.networking;

import com.streetart.ArtUtil;
import com.streetart.StreetArt;
import com.streetart.graffiti_data.TileChange;
import com.streetart.graffiti_data.TileKey;
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

}
