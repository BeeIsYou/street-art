package com.streetart;

import com.streetart.arealib.AreaLib;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

public class PermissionUtil {
    public static boolean modificationAllowed(final BlockPos pos, final Level level, final ItemStack stack, @Nullable final Player player) {
        boolean adventurePermitted = true;
        if (player == null || player.gameMode() == null || player.gameMode().isBlockPlacingRestricted()) {
            adventurePermitted = canPlaceOn(pos, level, stack) || StreetArt.AREA_LIB.isInRegion(level, pos, AreaLib.Type.MODIFYING_ALLOWED);
        }

        final boolean opped = player != null && Commands.LEVEL_ADMINS.check(player.permissions());

        if (StreetArt.AREA_LIB.isInRegion(level, pos, AreaLib.Type.PROTECTED)) {
            if (!adventurePermitted || !opped) {
                return false;
            }
        }

        return adventurePermitted;
    }

    public static boolean canPlaceOn(final BlockPos pos, final Level level, final ItemStack stack) {
        final AdventureModePredicate predicate = stack.get(DataComponents.CAN_PLACE_ON);
        return predicate != null && predicate.test(new BlockInWorld(level, pos, false));
    }
}
