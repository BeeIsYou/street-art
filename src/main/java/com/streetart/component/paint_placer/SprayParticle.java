package com.streetart.component.paint_placer;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum SprayParticle implements StringRepresentable {
    SPRAY(0, "spray"),
    WATER(1, "water");

    private final int id;
    private final String name;

    public static final Codec<SprayParticle> CODEC = StringRepresentable.fromEnum(SprayParticle::values);
    public static final IntFunction<SprayParticle> BY_ID = ByIdMap.continuous(
            p -> p.id, values(), ByIdMap.OutOfBoundsStrategy.CLAMP
    );
    public static final StreamCodec<ByteBuf, SprayParticle> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, p -> p.id);

    SprayParticle(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
