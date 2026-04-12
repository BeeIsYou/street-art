package com.streetart.client.debug;

import com.streetart.StreetArt;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.resources.Identifier;

public class AllDebugEntries {
    public static final Identifier SPRAY_PAINT_BLOCKS = DebugScreenEntries.register(
            StreetArt.id("spray_paint_blocks"),
            new DebugEntryNoop());
    public static final Identifier ATLAS_USAGE = DebugScreenEntries.register(
            StreetArt.id("atlas_usage"),
            new AtlasStatusEntry()
    );
}
