package com.streetart.component;

import com.mojang.serialization.Codec;
import com.streetart.AllDataComponents;
import com.streetart.AllItems;
import com.streetart.tracks.RecordedTrack;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public class TapeRecorderContents implements TooltipProvider {
    public static final Codec<TapeRecorderContents> CODEC = ItemStackTemplate.CODEC.xmap(TapeRecorderContents::new, contents -> contents.item);
    public static final StreamCodec<RegistryFriendlyByteBuf, TapeRecorderContents> STREAM_CODEC = ItemStackTemplate.STREAM_CODEC
            .map(TapeRecorderContents::new, contents -> contents.item);

    private final ItemStackTemplate item;

    public TapeRecorderContents(final ItemStackTemplate item) {
        this.item = item;
    }

    public TapeRecorderContents(final ItemStack item) {
        this.item = ItemStackTemplate.fromNonEmptyStack(item);
    }

    public ItemStack getContained() {
        return this.item.create();
    }

    public State getState() {
        final ItemStack contained = this.getContained();
        if (contained.isEmpty()) {
            return State.EMPTY;
        } else if (contained.is(AllItems.BLANK_TRACK)) {
            return State.BLANK;
        }
        return State.RECORDED;
    }

    public static boolean accepts(final ItemStack item) {
        return item.is(AllItems.BLANK_TRACK) || item.is(AllItems.TRACK);
    }

    @Override
    public void addToTooltip(final Item.TooltipContext context, final Consumer<Component> consumer, final TooltipFlag flag, final DataComponentGetter components) {
        final ItemLore lore = this.getContained().get(DataComponents.LORE);
        if (lore != null) {
            lore.addToTooltip(context, consumer, flag, components);
        }
        final RecordedTrack recording = this.getContained().get(AllDataComponents.TRACK_RECORDING);
        if (recording != null) {
            recording.addToTooltip(context, consumer, flag, components);
        }
    }

    public enum State implements StringRepresentable {
        EMPTY("empty"),
        BLANK("blank"),
        RECORDED("recorded")
        ;

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);

        private final String name;

        State(final String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
