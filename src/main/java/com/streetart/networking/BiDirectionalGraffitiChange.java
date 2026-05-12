package com.streetart.networking;

import com.streetart.ArtUtil;
import com.streetart.StreetArt;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.GraffitiChangeData;
import com.streetart.graffiti_data.GraffitiKey;
import com.streetart.graffiti_data.GraffitiLayerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;

public record BiDirectionalGraffitiChange(GraffitiLayerType layer, byte content, HashMap<GraffitiKey, GraffitiChangeData> changes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BiDirectionalGraffitiChange> TYPE  = new Type<>(StreetArt.id("graffiti_delta"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BiDirectionalGraffitiChange> CODEC = StreamCodec.composite(
            GraffitiLayerType.STREAM_CODEC, BiDirectionalGraffitiChange::layer,
            ByteBufCodecs.BYTE, BiDirectionalGraffitiChange::content,
            ByteBufCodecs.map(HashMap::new, GraffitiKey.CODEC, GraffitiChangeData.CODEC), BiDirectionalGraffitiChange::changes,
            BiDirectionalGraffitiChange::new
    );

    public static BiDirectionalGraffitiChange create(final GraffitiLayerType layer, final ColorComponent color) {
        return new BiDirectionalGraffitiChange(layer, color.id, new HashMap<>());
    }

    public ColorComponent color() {
        return ColorComponent.BY_ID.apply(this.content);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void markChanged(final BlockHitResult hitResult, final int x, final int y) {
        this.markChanged(hitResult.getBlockPos(), hitResult.getDirection(), ArtUtil.calculateDepth(hitResult), x, y);
    }

    public void markChanged(final BlockPos pos, final Direction dir, final int depth, final int x, final int y) {
        final GraffitiChangeData change = this.changes.computeIfAbsent(new GraffitiKey(pos, dir, depth), _ -> GraffitiChangeData.empty());
        change.markChanged(x, y);
    }
}
