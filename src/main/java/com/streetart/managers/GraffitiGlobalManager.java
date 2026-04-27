package com.streetart.managers;

import com.streetart.AttachmentTypes;
import com.streetart.graffiti_data.GraffitiChangeData;
import com.streetart.graffiti_data.GraffitiKey;
import com.streetart.networking.BiDirectionalGraffitiChange;
import com.streetart.networking.ServerBoundRequestDataPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Map;

public class GraffitiGlobalManager {
    public static void handleRequestPacket(final ServerBoundRequestDataPacket serverBoundRequestDataPacket, final ServerPlayNetworking.Context context) {
        final ServerLevel level = context.player().level();
        final ChunkPos pos = serverBoundRequestDataPacket.pos();

        final LevelChunk chunk = level.getChunk(pos.x(), pos.z());
        final GServerChunkManager manager = chunk.getAttached(AttachmentTypes.CHUNK_MANAGER);
        if (manager != null) {
            manager.handleRequest(context);
        }
    }

    public static void handleChange(final BiDirectionalGraffitiChange packet, final ServerPlayNetworking.Context context) {
        final ServerLevel level = context.player().level();
        for (final Map.Entry<GraffitiKey, GraffitiChangeData> entry : packet.changes().entrySet()) {
            final GraffitiKey key = entry.getKey();
            final GraffitiChangeData change = entry.getValue();

            final LevelChunk chunk = level.getChunkAt(key.pos());
            if (!chunk.getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER).handleChange(context.player(), packet, key, change)) {
                continue;
            }

            chunk.markUnsaved();
        }
    }
}
