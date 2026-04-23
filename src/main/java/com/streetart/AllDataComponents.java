package com.streetart;

import com.streetart.component.AreaSelectComponent;
import com.streetart.component.ChargeComponent;
import com.streetart.component.ColorComponent;
import com.streetart.component.RollerbladeComponent;
import com.streetart.tracks.TapeRecorderContents;
import com.streetart.tracks.TrackRecording;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.UnaryOperator;

public class AllDataComponents {
    public static final DataComponentType<ColorComponent> COLOR = register("content", builder -> builder
            .persistent(ColorComponent.CODEC).networkSynchronized(ColorComponent.STREAM_CODEC)
    );

    public static final DataComponentType<ChargeComponent> CHARGE = register("charge", builder -> builder
            .persistent(ChargeComponent.CODEC).networkSynchronized(ChargeComponent.STREAM_CODEC)
    );

    public static final DataComponentType<RollerbladeComponent> ROLLER_BLADES = register("roller_blade", builder -> builder
            .persistent(RollerbladeComponent.CODEC).networkSynchronized(RollerbladeComponent.STREAM_CODEC)
    );

    public static final DataComponentType<TapeRecorderContents> TAPE_RECORDER_CONTENTS = register("tape_recorder_contents", builder -> builder
            .persistent(TapeRecorderContents.CODEC).networkSynchronized(TapeRecorderContents.STREAM_CODEC));

    public static final DataComponentType<TrackRecording> TRACK_RECORDING = register("track_recording", builder -> builder
            .persistent(TrackRecording.CODEC).networkSynchronized(TrackRecording.STREAM_CODEC));

    public static final DataComponentType<AreaSelectComponent> AREA_SELECT = register("area_select", builder -> builder
            .networkSynchronized(AreaSelectComponent.BYTE_CODEC)
    );

    public static void init() {}

    private static <T> DataComponentType<T> register(final String id, final UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                StreetArt.id(id),
                ((DataComponentType.Builder)builder.apply(DataComponentType.builder())).build()
        );
    }
}
