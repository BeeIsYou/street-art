package com.streetart;

import com.streetart.graffiti_data.GraffitiLayerType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.BiPredicate;

public class AllGraffitiLayers {

    public static final ResourceKey<Registry<GraffitiLayerType>> GRAFFITI_LAYER_KEY = ResourceKey.createRegistryKey(StreetArt.id("graffiti_layer"));
    public static final Registry<GraffitiLayerType> LAYER_REGISTRY = FabricRegistryBuilder.create(GRAFFITI_LAYER_KEY).buildAndRegister();

    public static final GraffitiLayerType DEFAULT_LAYER = register("default", 0, (_, _) -> true);
    public static final GraffitiLayerType GLASSES_LAYER = register("glasses", 1,
            (p, _) -> p.getItemBySlot(EquipmentSlot.HEAD).is(AllItems.SWAG)
    );

    public static void init() {

    }

    public static GraffitiLayerType getActive(Player player, Level level) {
        GraffitiLayerType highest = null;
        for (GraffitiLayerType graffitiLayerType : LAYER_REGISTRY) {
            if (graffitiLayerType.visibility().test(player, level)) {
                if (highest == null) {
                    highest = graffitiLayerType;
                } else if (highest.renderingPriority() < graffitiLayerType.renderingPriority()) {
                    highest = graffitiLayerType;
                }
            }
        }
        // there should always be at least the default layer
        assert highest != null;
        return highest;
    }

    private static GraffitiLayerType register(final String id, final int renderingPriority, BiPredicate<Player, Level> visibility) {
        final Identifier graffitiLayerId = StreetArt.id(id);
        return Registry.register(
                LAYER_REGISTRY,
                graffitiLayerId,
                new GraffitiLayerType(graffitiLayerId, renderingPriority, visibility)
        );
    }
}
