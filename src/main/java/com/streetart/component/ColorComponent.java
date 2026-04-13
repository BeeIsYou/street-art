package com.streetart.component;

import com.mojang.serialization.Codec;
import com.streetart.AllDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntFunction;

public enum ColorComponent implements StringRepresentable {
    WHITE(     0,  "white"     , ARGB.opaque(16383998)),
    ORANGE(    1,  "orange"    , ARGB.opaque(16351261)),
    MAGENTA(   2,  "magenta"   , ARGB.opaque(13061821)),
    LIGHT_BLUE(3,  "light_blue", ARGB.opaque(3847130 )),
    YELLOW(    4,  "yellow"    , ARGB.opaque(16701501)),
    LIME(      5,  "lime"      , ARGB.opaque(8439583 )),
    PINK(      6,  "pink"      , ARGB.opaque(15961002)),
    GRAY(      7,  "gray"      , ARGB.opaque(4673362 )),
    LIGHT_GRAY(8,  "light_gray", ARGB.opaque(10329495)),
    CYAN(      9,  "cyan"      , ARGB.opaque(1481884 )),
    PURPLE(    10, "purple"    , ARGB.opaque(8991416 )),
    BLUE(      11, "blue"      , ARGB.opaque(3949738 )),
    BROWN(     12, "brown"     , ARGB.opaque(8606770 )),
    GREEN(     13, "green"     , ARGB.opaque(6192150 )),
    RED(       14, "red"       , ARGB.opaque(11546150)),
    BLACK(     15, "black"     , ARGB.opaque(1908001 )),
    CLEAR(     16, "clear"     , 0       );

    public static final Codec<ColorComponent> CODEC = StringRepresentable.fromValues(ColorComponent::values);
    public static final IntFunction<ColorComponent> BY_ID = ByIdMap.continuous(
            d -> d.id, values(), ByIdMap.OutOfBoundsStrategy.CLAMP
    );
    public static final StreamCodec<ByteBuf, ColorComponent> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, d -> d.id);

    public final byte id;
    public final String name;
    public final int argb;

    ColorComponent(final int id, final String name, final int argb) {
        this.id = (byte)id;
        this.name = name;
        this.argb = argb;
    }

    public static int getOrDefaultOpaque(final ItemStack stack, final int defaultColor) {
        final ColorComponent color = stack.get(AllDataComponents.COLOR);
        if (color == null || color == ColorComponent.CLEAR) {
            return defaultColor;
        }
        return color.argb;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
