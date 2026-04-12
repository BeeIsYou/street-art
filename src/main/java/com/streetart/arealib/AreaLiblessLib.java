package com.streetart.arealib;

import com.streetart.AllItems;
import com.streetart.SprayPaintInteractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class AreaLiblessLib {
    public boolean allowedToEdit(Player player, BlockPos pos) {
        return adventurePermits(player, pos);
    }

    public boolean decays(Level level, BlockPos pos) {
        return true;
    }

    public static boolean adventurePermits(Player player, BlockPos pos) {
        if (!player.gameMode().isBlockPlacingRestricted()) {
            return true;
        }

        BlockInWorld block = new BlockInWorld(player.level(), pos, false);
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof SprayPaintInteractor && permitsUsage(main, block)) {
            return true;
        }

        ItemStack offHand = player.getMainHandItem();
        if (offHand.getItem() instanceof SprayPaintInteractor && permitsUsage(offHand, block)) {
            return true;
        }

        return false;
    }

    public static boolean permitsUsage(ItemStack stack, BlockInWorld block) {
        AdventureModePredicate predicate = stack.get(DataComponents.CAN_PLACE_ON);
        return predicate != null && predicate.test(block);
    }
}
