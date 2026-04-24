package com.streetart.client;

import com.streetart.StreetArt;
import net.minecraft.client.color.item.ItemTintSources;

public class AllTintSources {
    public static void init() {
        ItemTintSources.ID_MAPPER.put(StreetArt.id("track_color"), TrackTintSource.MAP_CODEC);
    }
}
