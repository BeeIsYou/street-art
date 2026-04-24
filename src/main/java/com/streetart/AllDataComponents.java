package com.streetart;

import com.streetart.component.*;
import com.streetart.tracks.RecordedTrack;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;

import java.util.function.UnaryOperator;

public class AllDataComponents {
    public static final DataComponentType<ColorComponent> COLOR = register("content", builder -> builder
            .persistent(ColorComponent.CODEC).networkSynchronized(ColorComponent.STREAM_CODEC)
    );

    public static final DataComponentType<ChargeComponent> CHARGE = register("charge", builder -> builder
            .persistent(ChargeComponent.CODEC).networkSynchronized(ChargeComponent.STREAM_CODEC)
    );

    public static final DataComponentType<Integer> BUNDLE_STACK_SIZE_OVERRIDE = register("bundle_stack_size_override", builder -> builder
            .persistent(ExtraCodecs.intRange(1, 99)).networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    public static final DataComponentType<RollerbladeComponent> ROLLER_BLADES = register("roller_blade", builder -> builder
            .persistent(RollerbladeComponent.CODEC).networkSynchronized(RollerbladeComponent.STREAM_CODEC)
    );

    public static final DataComponentType<TapeRecorderContents> TAPE_RECORDER_CONTENTS = register("tape_recorder_contents", builder -> builder
            .persistent(TapeRecorderContents.CODEC).networkSynchronized(TapeRecorderContents.STREAM_CODEC));

    public static final DataComponentType<RecordedTrack> TRACK_RECORDING = register("track_recording", builder -> builder
            .persistent(RecordedTrack.CODEC).networkSynchronized(RecordedTrack.STREAM_CODEC));

    public static final DataComponentType<AreaSelectComponent> AREA_SELECT = register("area_select", builder -> builder
            .networkSynchronized(AreaSelectComponent.BYTE_CODEC)
    );

    public static void init() {
        ItemComponentTooltipProviderRegistry.addLast(TRACK_RECORDING);
        ItemComponentTooltipProviderRegistry.addLast(TAPE_RECORDER_CONTENTS);
    }

    private static <T> DataComponentType<T> register(final String id, final UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                StreetArt.id(id),
                ((DataComponentType.Builder)builder.apply(DataComponentType.builder())).build()
        );
    }
}
