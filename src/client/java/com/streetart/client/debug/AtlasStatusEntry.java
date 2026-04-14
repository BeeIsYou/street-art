package com.streetart.client.debug;

import com.streetart.client.StreetArtClient;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public class AtlasStatusEntry implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        int capacity = StreetArtClient.tileAtlasManager.getCapacity();
        int size = StreetArtClient.tileAtlasManager.getSize();
        displayer.addLine(
                "Spray Paint Atlas: " + size + "/" + capacity
        );
    }
}
