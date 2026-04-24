package com.streetart.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.AllDataComponents;
import com.streetart.component.TapeRecorderContents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record TapeRecorderContentsPropery(TapeRecorderContents.State state) implements SelectItemModelProperty<TapeRecorderContents.State> {
    public static final MapCodec<TapeRecorderContentsPropery> DATA_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TapeRecorderContents.State.CODEC.fieldOf("contents").forGetter(TapeRecorderContentsPropery::state)
    ).apply(instance, TapeRecorderContentsPropery::new));

    public static final SelectItemModelProperty.Type<TapeRecorderContentsPropery, TapeRecorderContents.State> TYPE = SelectItemModelProperty.Type.create(
            DATA_MAP_CODEC, TapeRecorderContents.State.CODEC
    );

    @Override
    public TapeRecorderContents.State get(final ItemStack itemStack, @Nullable final ClientLevel level, @Nullable final LivingEntity owner, final int seed, final ItemDisplayContext displayContext) {
        final TapeRecorderContents contents = itemStack.get(AllDataComponents.TAPE_RECORDER_CONTENTS);
        if (contents == null) {
            return TapeRecorderContents.State.EMPTY;
        }
        return contents.getState();
    }

    @Override
    public Codec<TapeRecorderContents.State> valueCodec() {
        return TapeRecorderContents.State.CODEC;
    }

    @Override
    public Type<? extends SelectItemModelProperty<TapeRecorderContents.State>, TapeRecorderContents.State> type() {
        return TYPE;
    }
}
