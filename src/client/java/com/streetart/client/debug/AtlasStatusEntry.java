package com.streetart.client.debug;

import com.streetart.client.StreetArtClient;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public class AtlasStatusEntry implements DebugScreenEntry {
    @Override
    public void display(final DebugScreenDisplayer displayer, @Nullable final Level serverOrClientLevel, @Nullable final LevelChunk clientChunk, @Nullable final LevelChunk serverChunk) {
        StreetArtClient.layers.forEach(((identifier, atlas) -> {
            final int capacity = atlas.getCapacity();
            final int size = atlas.getSize();
            displayer.addLine(
                    identifier + ": " + size + "/" + capacity
            );
        }));

    }
}
