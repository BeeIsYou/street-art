package com.streetart.managers;

import com.streetart.AttachmentTypes;
import com.streetart.StreetArt;
import com.streetart.networking.ServerBoundGraffitiUpdate;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.nio.ByteBuffer;

public class GraffitiGlobalManager {
    public static void handleServerUpdatePacket(final ServerBoundGraffitiUpdate packet, final ServerPlayNetworking.Context context) {
        if (!StreetArt.AREA_LIB.allowedToEdit(context.player(), packet.pos())) {
            // todo: specific packet tell of player?
            return;
        }

        for (final byte b : packet.textureData()) {
            if (b != 0) {
                // todo length validation

                final ServerLevel level = context.player().level();
                final ChunkAccess access = level.getChunk(packet.pos());

                final GServerChunkManager chunkManager = access.getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER);
                final GServerDataHolder data = chunkManager.getOrCreate(packet.pos(), packet.dir(), packet.depth());

                final ByteBuffer gData = data.getGraffitiData();
                gData.position(0);
                gData.put(packet.textureData());
                chunkManager.markDirty(data, packet.pos(), packet.dir());

                return;
            }
        }
        // todo mark specific thing removed
    }
}
