package com.streetart.tracks;

import com.mojang.serialization.Codec;
import com.streetart.AllItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class TapeRecorderContents {
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

    public static boolean accepts(final ItemStack item) {
        return item.is(AllItems.EMPTY_TRACK) || item.is(AllItems.TRACK);
    }
}
