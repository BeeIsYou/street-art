package com.streetart.graffiti_data;

import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

/**
 * @param graffityLayerId
 * @param depth           The depth of this layer. higher depths will be rendered first.
 * @param requiredItem    The required item to see this layer. Can be null if layer is always visible.
 */
public record GraffitiLayerType(net.minecraft.resources.Identifier graffityLayerId, int depth, @Nullable ItemLike requiredItem) {
}
