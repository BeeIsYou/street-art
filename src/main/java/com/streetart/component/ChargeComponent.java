package com.streetart.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.AllDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public record ChargeComponent(int amount, int max) {
    public static final Codec<ChargeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("amount", 0).forGetter(ChargeComponent::amount),
            Codec.INT.fieldOf("max").forGetter(ChargeComponent::max)
    ).apply(instance, ChargeComponent::new));

    public static final StreamCodec<ByteBuf, ChargeComponent> BYTE_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ChargeComponent::amount,
            ByteBufCodecs.INT,
            ChargeComponent::max,
            ChargeComponent::new
    );

    public static void delta(final ItemStack stack, final int delta) {
        final ChargeComponent component = stack.get(AllDataComponents.CHARGE);
        if (component == null) {
            return;
        }

        stack.set(AllDataComponents.CHARGE, new ChargeComponent(
                Mth.clamp(component.amount() + delta, 0, component.max()),
                component.max()
        ));
    }

    public static int get(final ItemStack stack) {
        final ChargeComponent component = stack.get(AllDataComponents.CHARGE);
        if (component == null) {
            return 0;
        }

        return component.amount;
    }

    public static int width(final ItemStack stack) {
        final ChargeComponent component = stack.get(AllDataComponents.CHARGE);
        return Mth.clamp(Math.round(component.amount * 13.0F / component.max), 0, 13);
    }
}
