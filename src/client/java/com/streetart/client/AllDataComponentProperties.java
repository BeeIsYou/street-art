package com.streetart.client;

import com.streetart.StreetArt;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;

public class AllDataComponentProperties {
    public static void init() {
        SelectItemModelProperties.ID_MAPPER.put(StreetArt.id("tape_recorder_contents"), TapeRecorderContentsPropery.TYPE);
    }
}
