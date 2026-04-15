package com.streetart;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class AllTags {
    public static class Items {
        public static final TagKey<Item> SPRAY_CANS = create("spray_cans");
        public static final TagKey<Item> PAINT_BALLOONS = create("paint_balloons");
        public static final TagKey<Item> PAINT_CREATORS = create("paint_creators");

        private static TagKey<Item> create(final String id) {
            return TagKey.create(Registries.ITEM, StreetArt.id(id));
        }
    }

}
