package com.streetart.components;

import com.mojang.serialization.Codec;
import com.streetart.AllDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record Focused(boolean b) implements TooltipProvider {
    public static final Codec<Focused> CODEC = Codec.BOOL.xmap(Focused::new, Focused::b);
    public static final StreamCodec<ByteBuf, Focused> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, Focused::b, Focused::new);

    public static final Focused FALSE = new Focused(false);
    public static final Focused TRUE = new Focused(true);

    public static boolean is(DataComponentGetter itemStack) {
        Focused component = itemStack.get(AllDataComponents.FOCUSED);
        return component != null && component.b;
    }

    public static boolean toggle(ItemStack itemStack) {
        boolean nextState = !is(itemStack);
        itemStack.set(AllDataComponents.FOCUSED, nextState ?  TRUE : FALSE);
        return nextState;
    }

    public static Component KEY_FOCUSED = Component.translatable("street_art.spray_can.focused");
    public static Component KEY_UNFOCUSED = Component.translatable("street_art.spray_can.unfocused");

    public static MutableComponent getKey(DataComponentGetter itemStack) {
        return is(itemStack) ?
                Component.translatable("street_art.spray_can.focused") :
                Component.translatable("street_art.spray_can.unfocused");
    }

    public MutableComponent getKey() {
        return this.b ?
                Component.translatable("street_art.spray_can.focused") :
                Component.translatable("street_art.spray_can.unfocused");
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        consumer.accept(this.getKey().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
