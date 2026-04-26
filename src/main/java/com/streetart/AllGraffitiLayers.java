package com.streetart;

import com.streetart.graffiti_data.GraffitiLayerType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class AllGraffitiLayers {

    public static final ResourceKey<Registry<GraffitiLayerType>> GRAFFITI_LAYER_KEY = ResourceKey.createRegistryKey(StreetArt.id("graffiti_layer"));
    public static final Registry<GraffitiLayerType> LAYER_REGISTRY = FabricRegistryBuilder.create(GRAFFITI_LAYER_KEY).buildAndRegister();

    public static final GraffitiLayerType DEFAULT_LAYER = register("default", 0, null);

    public static void init() {

    }

    private static GraffitiLayerType register(final String id, final int depth, @Nullable final ItemLike requiredItem) {
        final Identifier graffitiLayerId = StreetArt.id(id);
        return Registry.register(
                LAYER_REGISTRY,
                graffitiLayerId,
                new GraffitiLayerType(graffitiLayerId, depth, requiredItem)
        );
    }
}
