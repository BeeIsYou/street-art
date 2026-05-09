package com.streetart.item;

import com.streetart.AllDataComponents;
import com.streetart.AttachmentTypes;
import com.streetart.component.ChargeComponent;
import com.streetart.managers.GServerChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class CreativePressureWasherItem extends Item {
    public CreativePressureWasherItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            ChargeComponent.delta(player.getItemInHand(hand), 1);
        } else {
            ChargeComponent.delta(player.getItemInHand(hand), -1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        return ItemUtils.startUsingInstantly(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public int getUseDuration(final ItemStack itemStack, final LivingEntity user) {
        return 1200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(final ItemStack itemStack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public void onUseTick(final Level level, final LivingEntity livingEntity, final ItemStack itemStack, final int ticksRemaining) {
        if (livingEntity instanceof final ServerPlayer player) {
            final HitResult h = player.pick(player.blockInteractionRange(), 1, false);
            if (h.getType() != HitResult.Type.MISS && h instanceof final BlockHitResult hit) {
                final int charge = ChargeComponent.get(itemStack);
                BlockPos.betweenClosed(
                        hit.getBlockPos().offset(-charge, -charge, -charge),
                        hit.getBlockPos().offset(charge, charge, charge)
                ).forEach(blockPos -> {
                    final GServerChunkManager manager = player.level().getChunk(blockPos).getAttached(AttachmentTypes.CHUNK_MANAGER);
                    if (manager != null) {
                        manager.markForRemoval(blockPos.immutable());
                    }
                });
            }
        }
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        final ChargeComponent component = stack.get(AllDataComponents.CHARGE);
        final boolean charged = component != null && component.amount() > 0;
        return charged || super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        if (stack.has(AllDataComponents.CHARGE)) {
            return ChargeComponent.width(stack);
        }
        return super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        return DyeColor.LIGHT_BLUE.getTextureDiffuseColor();
    }
}
