package com.streetart.managers.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record TempData(GServerDataHolder data, BlockPos pos, Direction dir) {
}
