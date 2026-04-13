package com.streetart.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.component.ColorComponent;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record ColorComponentTint(int defaultColor) implements ItemTintSource {
    public static final MapCodec<ColorComponentTint> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(ColorComponentTint::defaultColor)
            ).apply(i, ColorComponentTint::new)
    );

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return ColorComponent.getOrDefaultOpaque(itemStack, this.defaultColor);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
