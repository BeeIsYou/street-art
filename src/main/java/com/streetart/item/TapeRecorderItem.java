package com.streetart.item;

import com.streetart.AllItems;
import com.streetart.StreetArt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TapeRecorderItem extends Item {
    public TapeRecorderItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (level.isClientSide()) {
            StreetArt.recordingManager.itemUse(player);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canDestroyBlock(final ItemStack itemStack, final BlockState state, final Level level, final BlockPos pos, final LivingEntity user) {
        return super.canDestroyBlock(itemStack, state, level, pos, user);
    }

    public static boolean hasRecorder(final Player player) {
        return player.getInventory().contains(stack -> stack.is(AllItems.TAPE_RECORDER));
    }
}
