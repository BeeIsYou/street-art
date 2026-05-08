package com.streetart.component.paint_placer;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.function.IntFunction;

public enum SprayType implements StringRepresentable {
    POINT(0, "point"),
    CONE(1, "cone"),
    HORIZONTAL(2, "horizontal"),
    VERTICAL(3, "vertical");

    public static final Codec<SprayType> CODEC = StringRepresentable.fromEnum(SprayType::values);
    public static final IntFunction<SprayType> BY_ID = ByIdMap.continuous(
            c -> c.id, values(), ByIdMap.OutOfBoundsStrategy.CLAMP
    );
    public static final StreamCodec<ByteBuf, SprayType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, c -> c.id);

    private final int id;
    private final String name;

    SprayType(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Vec3 getLookVector(final Player player, final Vec2 originalRot, final Vec3 forward, final float pt, final double scale) {
        return switch (this) {
            case POINT -> forward;
            case CONE -> {
                final Vec3 up = player.calculateViewVector(originalRot.x + 90, originalRot.y);
                final Vec3 left = forward.cross(up);

                final double dx = player.getRandom().nextGaussian() * 0.04 * scale;
                final double dy = player.getRandom().nextGaussian() * 0.04 * scale;

                yield forward
                        .add(left.scale(dx))
                        .add(up.scale(dy))
                        .normalize();
            }
            case HORIZONTAL -> {
                final Vec3 up = player.calculateViewVector(originalRot.x + 90, originalRot.y);
                final Vec3 left = forward.cross(up);

                final double dx = player.getRandom().nextDouble() * 0.25  - 0.125;
                final double dy = player.getRandom().nextDouble() * 0.05 - 0.025;

                yield forward
                        .add(left.scale(dx))
                        .add(up.scale(dy))
                        .normalize();
            }
            case VERTICAL -> {
                final Vec3 up = player.calculateViewVector(originalRot.x + 90, originalRot.y);
                final Vec3 left = forward.cross(up);

                final double dx = player.getRandom().nextDouble() * 0.05 - 0.025;
                final double dy = player.getRandom().nextDouble() * 0.25  - 0.125;

                yield forward
                        .add(left.scale(dx))
                        .add(up.scale(dy))
                        .normalize();
            }
        };
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
