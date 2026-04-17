package com.streetart.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.StreetArt;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

// why are armour layers an enum grrrrr
public record RollerbladeComponent(Identifier texture) {
    public static final Codec<RollerbladeComponent> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Identifier.CODEC.fieldOf("texture").forGetter(RollerbladeComponent::texture)
        ).apply(instance, RollerbladeComponent::new)
    );

    public static final StreamCodec<ByteBuf, RollerbladeComponent> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            RollerbladeComponent::texture,
            RollerbladeComponent::new
    );

    public static RollerbladeComponent streetArt(final String texture) {
        return new RollerbladeComponent(StreetArt.id("textures/armor/" + texture + ".png"));
    }
}
