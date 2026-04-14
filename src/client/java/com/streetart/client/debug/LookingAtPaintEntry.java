package com.streetart.client.debug;

import com.streetart.client.StreetArtClient;
import com.streetart.client.manager.GClientBlock;
import com.streetart.client.manager.GClientData;
import com.streetart.client.manager.GClientManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LookingAtPaintEntry implements DebugScreenEntry {
    private static final int RANGE = 20;

    @Override
    public void display(final DebugScreenDisplayer displayer, @Nullable final Level serverOrClientLevel, @Nullable final LevelChunk clientChunk, @Nullable final LevelChunk serverChunk) {
        final Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity != null) {
            final HitResult result = cameraEntity.pick(RANGE, 0, false);

            if (result instanceof final BlockHitResult hitResult) {
                final BlockPos pos = hitResult.getBlockPos();

                GClientManager manager = StreetArtClient.textureManager.get(ChunkPos.containing(pos));
                if (manager != null) {
                    final GClientBlock block = manager.getGraffiti().get(pos);

                    if (block != null) {
                        final List<String> lines = new ArrayList<>();
                        lines.add(ChatFormatting.UNDERLINE + "Targeted Paint: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());

                        block.getBlockData().forEach((dir, datas) -> {
                            lines.add(dir.toString());
                        for (final GClientData data : datas) {
                            lines.add(String.format(" %d, (%.4f)", data.id, data.getDepth()));
                        }
                        });

                        displayer.addToGroup(AllDebugEntries.LOOKING_AT_PAINT, lines);
                    }
                }
            }
        }
    }
}
