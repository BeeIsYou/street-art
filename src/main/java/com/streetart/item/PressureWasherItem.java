package com.streetart.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

public class PressureWasherItem extends Item {
    public PressureWasherItem(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(final ItemStack itemStack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public InteractionResult interactLivingEntity(final ItemStack itemStack, final Player player, final LivingEntity target, final InteractionHand type) {
        if (!player.level().isClientSide()) {
            if (target instanceof Blaze) {
                target.hurtServer((ServerLevel) player.level(), target.damageSources().drown(), 2);
            }
        }

        return super.interactLivingEntity(itemStack, player, target, type);
    }
}
