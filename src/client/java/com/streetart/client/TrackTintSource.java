package com.streetart.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.AllDataComponents;
import com.streetart.component.TapeRecorderContents;
import com.streetart.tracks.RecordedTrack;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record TrackTintSource(int defaultColor, boolean first) implements ItemTintSource {
    public static final MapCodec<TrackTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(TrackTintSource::defaultColor),
            Codec.BOOL.fieldOf("first").forGetter(TrackTintSource::first)
    ).apply(i, TrackTintSource::new));

    @Override
    public int calculate(ItemStack itemStack, @Nullable final ClientLevel level, @Nullable final LivingEntity owner) {
        final TapeRecorderContents contents = itemStack.get(AllDataComponents.TAPE_RECORDER_CONTENTS);
        if (contents != null) {
            itemStack = contents.getContained();
        }

        final RecordedTrack track = itemStack.get(AllDataComponents.TRACK_RECORDING);
        if (track != null) {
            if (this.first) {
                return track.colorA.getTextureDiffuseColor();
            } else {
                return track.colorB.getTextureDiffuseColor();
            }
        }
        return this.defaultColor;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
