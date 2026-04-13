package com.streetart.item;

import com.streetart.ArtUtil;
import com.streetart.AttachmentTypes;
import com.streetart.graffiti_data.TileKey;
import com.streetart.managers.GServerChunkManager;
import com.streetart.managers.GServerDataHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.List;

public class PaintBalloonItem extends Item {
    public PaintBalloonItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        if (context.getLevel() instanceof final ServerLevel serverLevel) {
            final BlockState block = serverLevel.getBlockState(context.getClickedPos());
            final List<ArtUtil.ShapeFaces> shapeFaces = ArtUtil.doThingsWithVoxelShape(block.getShape(serverLevel, context.getClickedPos()));
            final ChunkAccess chunk = serverLevel.getChunk(context.getClickedPos());
            final GServerChunkManager manager = chunk.getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER);
            for (final ArtUtil.ShapeFaces faces : shapeFaces) {
                faces.forEach((dir, face) -> {
                    final TileKey key = new TileKey(context.getClickedPos(), dir, face.depth());
                    GServerDataHolder data = manager.getOrCreate(key.pos(), key.dir(), key.depth());
                    data.fillFromTo(-1, face.x1(), face.y1(), face.x2(), face.y2());
                    manager.markDirty(data, context.getClickedPos(), dir);
                });
            }
            chunk.markUnsaved();
        }

        return InteractionResult.SUCCESS;
    }
}
