package com.streetart.managers.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public record ExposedGraffitiData(@Nullable Identifier layer, @Nullable GServerDataHolder data, BlockPos pos, @Nullable Direction dir) {
}
