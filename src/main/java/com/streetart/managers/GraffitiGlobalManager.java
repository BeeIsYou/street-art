package com.streetart.managers;

import com.streetart.AttachmentTypes;
import com.streetart.StreetArt;
import com.streetart.graffiti_data.TileChange;
import com.streetart.graffiti_data.TileKey;
import com.streetart.networking.BiDirectionalGraffitiChange;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ServerBoundGraffitiUpdate;
import com.streetart.networking.ServerBoundRequestDataPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

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

                access.markUnsaved();
                chunkManager.markDirty(data, packet.pos(), packet.dir());

                return;
            }
        }
        // todo mark specific thing removed
    }


    public static void handleRequestPacket(final ServerBoundRequestDataPacket serverBoundRequestDataPacket, final ServerPlayNetworking.Context context) {
        final ServerLevel level = context.player().level();
        final ChunkPos pos = serverBoundRequestDataPacket.pos();

        final LevelChunk chunk = level.getChunk(pos.x(), pos.z());
        final GServerChunkManager manager = chunk.getAttached(AttachmentTypes.CHUNK_MANAGER);
        if (manager != null) {
            for (final GServerBlock value : manager.getGraffiti().values()) {
                for (final Map.Entry<Direction, List<GServerDataHolder>> entries : value.getBlockData().entrySet()) {
                    for (final GServerDataHolder holder : entries.getValue()) {
                        ServerPlayNetworking.send(context.player(), new ClientBoundGraffitiSet(
                                value.getBlockPos(),
                                entries.getKey(),
                                holder.getDepth(),
                                holder.getGraffitiData().array()
                        ));
                    }
                }
            }
        }
    }

    public static void handleChange(BiDirectionalGraffitiChange packet, ServerPlayNetworking.Context context) {
        final ServerLevel level = context.player().level();
        for (Map.Entry<TileKey, TileChange> entry : packet.changes().entrySet()) {
            TileKey key = entry.getKey();
            TileChange change = entry.getValue();

            final LevelChunk chunk = level.getChunkAt(key.pos());
            final GServerChunkManager manager = chunk.getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER);

            GServerDataHolder tile = manager.getOrCreate(key.pos(), key.dir(), key.depth());
            tile.handleChange(packet.color(), change);
            manager.addPatch(packet);

            chunk.markUnsaved();
//            manager.markDirty(tile, key.pos(), key.dir());
        }
    }
}
