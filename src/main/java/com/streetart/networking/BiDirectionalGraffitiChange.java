package com.streetart.networking;

import com.streetart.ArtUtil;
import com.streetart.StreetArt;
import com.streetart.component.ColorComponent;
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

public record BiDirectionalGraffitiChange(byte content, HashMap<TileKey, TileChange> changes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BiDirectionalGraffitiChange> TYPE  = new Type<>(StreetArt.id("graffiti_delta"));
    public static final StreamCodec<ByteBuf, BiDirectionalGraffitiChange> CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            BiDirectionalGraffitiChange::content,
            ByteBufCodecs.map(HashMap::new, TileKey.CODEC, TileChange.CODEC),
            BiDirectionalGraffitiChange::changes,
            BiDirectionalGraffitiChange::new
    );

    public static BiDirectionalGraffitiChange create(final ColorComponent color) {
        return new BiDirectionalGraffitiChange(color.id, new HashMap<>());
    }

    public ColorComponent color() {
        return ColorComponent.BY_ID.apply(this.content);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void markChanged(final BlockPos pos, final Direction dir, final int depth, final int x, final int y) {
        final TileChange change = this.changes.computeIfAbsent(new TileKey(pos, dir, depth), _ -> TileChange.empty());
        change.markChanged(x, y);
    }

    public void markChanged(final BlockHitResult hitResult, final int x, final int y) {
        this.markChanged(hitResult.getBlockPos(), hitResult.getDirection(), ArtUtil.calculateDepth(hitResult), x, y);
    }
}
