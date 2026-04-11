package com.streetart;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class AllItems {
    public static SprayCanItem sprayCanItem = register("spray_can", SprayCanItem::new, new Item.Properties().stacksTo(1));

    public static void init() {}

    private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, StreetArt.id(name));

        T item = factory.apply(properties.setId(key));

        Registry.register(BuiltInRegistries.ITEM, key, item);

        return item;
    }
}
