package com.streetart.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface SprayPaintInteractor {
    Vec3 getLookVector(Player player, Vec2 originalRot, Vec3 forward, ItemStack itemStack, float pt, boolean rightClick);

    int iterationsPerTick(Player player, ItemStack itemStack);

    boolean hasColor(Player player, ItemStack itemStack);

    int getColor(Player player, ItemStack itemStack);
}
