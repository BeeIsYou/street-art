package com.streetart.managers;

import com.streetart.AttachmentTypes;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.TileChange;
import com.streetart.graffiti_data.TileKey;
import com.streetart.networking.BiDirectionalGraffitiChange;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ServerBoundRequestDataPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;
import java.util.Map;

public class GraffitiGlobalManager {
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

    public static void handleChange(final BiDirectionalGraffitiChange packet, final ServerPlayNetworking.Context context) {
        final ServerLevel level = context.player().level();
        for (final Map.Entry<TileKey, TileChange> entry : packet.changes().entrySet()) {
            final TileKey key = entry.getKey();
            final TileChange change = entry.getValue();

            final LevelChunk chunk = level.getChunkAt(key.pos());
            final GServerChunkManager manager = chunk.getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER);

            final GServerDataHolder tile = manager.getOrConditionalCreate(
                    key.pos(),
                    key.dir(),
                    key.depth(),
                    packet.content() == ColorComponent.CLEAR.id
            );

            if (tile == null) {
                continue;
            }

            if (tile.handleChange(packet.content(), change)) {
                manager.tryRemoveData(key.pos(), key.dir(), key.depth());
            }

            if (packet.content() != 0) {
                tile.refreshGrace();
                manager.addPatch(packet);
            } else {
                if (!manager.removeIfEmpty(key.pos())) {
                    manager.addPatch(packet);
                }
            }

            chunk.markUnsaved();
//            manager.markDirty(tile, key.pos(), key.dir());
        }
    }
}
