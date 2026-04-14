package com.streetart.component;

import com.mojang.serialization.Codec;
import com.streetart.AllDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntFunction;

public enum ColorComponent implements StringRepresentable {
    CLEAR(0, "clear", 0),
    WHITE(1, "white", ARGB.opaque(16383998)),
    ORANGE(2, "orange", ARGB.opaque(16351261)),
    MAGENTA(3, "magenta", ARGB.opaque(13061821)),
    LIGHT_BLUE(4, "light_blue", ARGB.opaque(3847130)),
    YELLOW(5, "yellow", ARGB.opaque(16701501)),
    LIME(6, "lime", ARGB.opaque(8439583)),
    PINK(7, "pink", ARGB.opaque(15961002)),
    GRAY(8, "gray", ARGB.opaque(4673362)),
    LIGHT_GRAY(9, "light_gray", ARGB.opaque(10329495)),
    CYAN(10, "cyan", ARGB.opaque(1481884)),
    PURPLE(11, "purple", ARGB.opaque(8991416)),
    BLUE(12, "blue", ARGB.opaque(3949738)),
    BROWN(13, "brown", ARGB.opaque(8606770)),
    GREEN(14, "green", ARGB.opaque(6192150)),
    RED(15, "red", ARGB.opaque(11546150)),
    BLACK(16, "black", ARGB.opaque(1908001));

    public static final Codec<ColorComponent> CODEC = StringRepresentable.fromValues(ColorComponent::values);
    public static final IntFunction<ColorComponent> BY_ID = ByIdMap.continuous(
            d -> d.id, values(), ByIdMap.OutOfBoundsStrategy.CLAMP
    );
    public static final StreamCodec<ByteBuf, ColorComponent> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, d -> d.id);

    public final byte id;
    public final String name;
    public final int argb;

    ColorComponent(final int id, final String name, final int argb) {
        this.id = (byte) id;
        this.name = name;
        this.argb = argb;
    }

    public static ColorComponent fromDye(final DyeColor color) {
        return values()[color.ordinal() + 1];
    }

    public static int getOrDefaultOpaque(final ItemStack stack, final int defaultColor) {
        final ColorComponent color = stack.get(AllDataComponents.COLOR);
        if (color == null || color == ColorComponent.CLEAR) {
            return defaultColor;
        }

        return color.argb;
    }

    public static ColorComponent getOrDefaultComponent(final ItemStack stack, final ColorComponent defaultComp) {
        final ColorComponent color = stack.get(AllDataComponents.COLOR);
        if (color == null || color == ColorComponent.CLEAR) {
            return defaultComp;
        }

        return color;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
