package com.streetart.component.paint_placer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.component.ColorComponent;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record PaintPlacerComponent(Optional<Spray> rightClick, Optional<Spray> leftClick,
                                   Vec3 firstPersonOffset, Vec2 thirdPersonOffset) {
    public static final Codec<PaintPlacerComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Spray.CODEC.optionalFieldOf("rightClick").forGetter(PaintPlacerComponent::rightClick),
            Spray.CODEC.optionalFieldOf("leftClick").forGetter(PaintPlacerComponent::leftClick),
            Vec3.CODEC.fieldOf("firstPerson").forGetter(PaintPlacerComponent::firstPersonOffset),
            Vec2.CODEC.fieldOf("thirdPerson").forGetter(PaintPlacerComponent::thirdPersonOffset)
    ).apply(instance, PaintPlacerComponent::new));

    // mojang why...
    private static final StreamCodec<ByteBuf, Vec2> VEC2_STREAM_CODEC = new StreamCodec<>() {
        public Vec2 decode(final ByteBuf input) {
            return new Vec2(input.readFloat(), input.readFloat());
        }

        public void encode(final ByteBuf output, final Vec2 value) {
            output.writeFloat(value.x);
            output.writeFloat(value.y);
        }
    };

    public static final StreamCodec<ByteBuf, PaintPlacerComponent> STREAM_CODEC = StreamCodec.composite(
            Spray.STREAM_CODEC.apply(ByteBufCodecs::optional), PaintPlacerComponent::rightClick,
            Spray.STREAM_CODEC.apply(ByteBufCodecs::optional), PaintPlacerComponent::leftClick,
            Vec3.STREAM_CODEC, PaintPlacerComponent::firstPersonOffset,
            VEC2_STREAM_CODEC, PaintPlacerComponent::thirdPersonOffset,
            PaintPlacerComponent::new
    );

    public static PaintPlacerComponent sprayCan(final ColorComponent color) {
        return new PaintPlacerComponent(
            Optional.of(new Spray(
                SprayType.CONE,
                32,
                1,
                color,
                Optional.of(SprayParticle.SPRAY)
            )),
            Optional.of(new Spray(
                SprayType.CONE,
                32,
                0.2,
                color,
                Optional.of(SprayParticle.SPRAY)
            )),
            new Vec3(0.525, -0.1, 1),
            new Vec2(0.35f, 0.8f)
        );
    }

    public static PaintPlacerComponent pressureWasher() {
        return new PaintPlacerComponent(
            Optional.of(new Spray(
                SprayType.HORIZONTAL,
                64,
                1,
                ColorComponent.CLEAR,
                Optional.of(SprayParticle.WATER)
            )),
            Optional.of(new Spray(
                SprayType.VERTICAL,
                64,
                1,
                ColorComponent.CLEAR,
                Optional.of(SprayParticle.WATER)
            )),
            new Vec3(0.25, -0.05, 2),
            new Vec2(0.35f, 1.6f)
        );
    }

    public @Nullable Spray getSpray(final boolean rightClick) {
        if (rightClick) {
            return this.rightClick.orElse(null);
        } else {
            return this.leftClick.orElse(null);
        }
    }
}
