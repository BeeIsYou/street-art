package com.streetart.graffiti_data;

import com.streetart.AllGraffitiLayers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * @param renderingPriority           The depth of this layer. higher depths will be rendered first.
 */
public record GraffitiLayerType(Identifier identifier, int renderingPriority,
                                java.util.function.BiPredicate<Player, Level> visibility) {

    public static final StreamCodec<RegistryFriendlyByteBuf, GraffitiLayerType> STREAM_CODEC =
            ByteBufCodecs.registry(AllGraffitiLayers.GRAFFITI_LAYER_KEY);
}
