package com.streetart.item;

import com.streetart.AttachmentTypes;
import com.streetart.managers.GServerChunkManager;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class PressureWasherItem extends Item {
    public PressureWasherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (Commands.LEVEL_ADMINS.check(player.permissions())) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return 1200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        if (livingEntity instanceof ServerPlayer player) {
            if (Commands.LEVEL_ADMINS.check(player.permissions())) {
                HitResult h = player.pick(player.blockInteractionRange(), 1, false);
                if (h.getType() != HitResult.Type.MISS && h instanceof BlockHitResult hit) {
                    GServerChunkManager manager = player.level().getChunk(hit.getBlockPos()).getAttached(AttachmentTypes.CHUNK_MANAGER);
                    manager.markForRemoval(hit.getBlockPos());
                }
            }
        }
    }
}
