package com.streetart;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.UnaryOperator;

public class AllDataComponents {
    public static void init() {}

    private static <T> DataComponentType<T> register(String id, final UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                StreetArt.id(id),
                ((DataComponentType.Builder)builder.apply(DataComponentType.builder())).build()
        );
    }
}
