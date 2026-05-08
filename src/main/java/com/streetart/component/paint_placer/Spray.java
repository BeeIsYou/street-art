package com.streetart.component.paint_placer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.component.ColorComponent;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record Spray(SprayType type, int iterations, double scale, ColorComponent color, Optional<SprayParticle> particle) {
    public static final Codec<Spray> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SprayType.CODEC.fieldOf("type").forGetter(Spray::type),
            Codec.INT.fieldOf("iterations").forGetter(Spray::iterations),
            Codec.DOUBLE.fieldOf("scale").forGetter(Spray::scale),
            ColorComponent.CODEC.fieldOf("color").forGetter(Spray::color),
            SprayParticle.CODEC.optionalFieldOf("particle").forGetter(Spray::particle)
    ).apply(instance, Spray::new));

    public static final StreamCodec<ByteBuf, Spray> STREAM_CODEC = StreamCodec.composite(
            SprayType.STREAM_CODEC, Spray::type,
            ByteBufCodecs.INT, Spray::iterations,
            ByteBufCodecs.DOUBLE, Spray::scale,
            ColorComponent.STREAM_CODEC, Spray::color,
            SprayParticle.STREAM_CODEC.apply(ByteBufCodecs::optional), Spray::particle,
            Spray::new
    );

    public Vec3 getLookVector(final Player player, final Vec2 originalRot, final Vec3 forward, final float pt) {
        return this.type.getLookVector(player, originalRot, forward, pt, this.scale);
    }

    public @Nullable ParticleOptions getParticle() {
        if (this.particle.isPresent()) {
            return switch (this.particle.get()) {
                case SPRAY -> this.color == ColorComponent.CLEAR ? null : new DustParticleOptions(this.color.argb, 1);
                case WATER -> ParticleTypes.FALLING_WATER;
            };
        }
        return null;
    }
}