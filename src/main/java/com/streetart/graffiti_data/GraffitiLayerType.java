package com.streetart.graffiti_data;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * @param renderingPriority           The depth of this layer. higher depths will be rendered first.
 */
public record GraffitiLayerType(Identifier identifier, int renderingPriority,
                                java.util.function.BiPredicate<Player, Level> visibility) {
}
