package com.streetart.networking;

import com.streetart.StreetArt;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;

public record ServerBoundRequestDataPacket(ChunkPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerBoundRequestDataPacket> TYPE  = new Type<>(StreetArt.id("client_graffiti_request"));

    public static final StreamCodec<ByteBuf, ServerBoundRequestDataPacket> CODEC = StreamCodec.composite(
            ChunkPos.STREAM_CODEC, ServerBoundRequestDataPacket::pos,
            ServerBoundRequestDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
