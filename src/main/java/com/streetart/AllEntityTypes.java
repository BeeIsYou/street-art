package com.streetart;

import com.streetart.entity.PaintBalloon;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class AllEntityTypes {
    public static final EntityType<PaintBalloon> PAINT_BALLOON = register("paint_balloon",
            EntityType.Builder.<PaintBalloon>of(PaintBalloon::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10)
    );

    public static void init() {}

    private static <T extends Entity> EntityType<T> register(final String id, final EntityType.Builder<T> builder) {
        final ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, StreetArt.id(id));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }
}
