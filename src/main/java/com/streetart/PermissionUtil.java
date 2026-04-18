package com.streetart;

import com.streetart.arealib.AreaLib;
import com.streetart.networking.ClientBoundGameRuleSync;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

public class PermissionUtil {
    public static boolean modificationAllowed(final BlockPos pos, final Level level, final ItemStack stack, @Nullable final Player player) {
        final boolean adventurePermitted = getAdventurePermitted(pos, level, stack, player);

        final boolean opped =
                player != null &&
                player.gameMode() != null &&
                player.gameMode().isCreative() &&
                Commands.LEVEL_ADMINS.check(player.permissions());

        if (StreetArt.AREA_LIB.isInRegion(level, pos, AreaLib.Type.PROTECTED)) {
            if (!adventurePermitted || !opped) {
                return false;
            }
        }

        return adventurePermitted;
    }

    public static boolean getAdventurePermitted(final BlockPos pos, final Level level, final ItemStack stack, @Nullable final Player player) {
        if (ClientBoundGameRuleSync.get(level).adventurePainting()) {
            return true;
        }

        if (player == null) {
            if (level instanceof ServerLevel serverLevel) {
                if (!serverLevel.getGameRules().get(AllGameRules.NON_PLAYERS_ADVENTURE)) {
                    return true;
                }
            }
        }

        if (player == null || player.gameMode() == null || player.gameMode().isBlockPlacingRestricted()) {
            return canPlaceOn(pos, level, stack) || StreetArt.AREA_LIB.isInRegion(level, pos, AreaLib.Type.MODIFYING_ALLOWED);
        }

        return true;
    }

    public static boolean canPlaceOn(final BlockPos pos, final Level level, final ItemStack stack) {
        final AdventureModePredicate predicate = stack.get(DataComponents.CAN_PLACE_ON);
        return predicate != null && predicate.test(new BlockInWorld(level, pos, false));
    }
}
