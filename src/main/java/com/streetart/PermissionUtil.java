package com.streetart;

import com.streetart.arealib.AreaLib;
import com.streetart.networking.ClientBoundGameRuleSync;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

public class PermissionUtil {
    public static boolean isOpped(final Player player) {
        return player.gameMode() != null &&
                player.gameMode().isCreative() &&
                Commands.LEVEL_GAMEMASTERS.check(player.permissions());
    }

    public static boolean sprayingAllowed(final BlockPos pos, final Level level, final ItemStack stack, @Nullable final Player player, final Identifier activeLayer) {
        final boolean adventurePermitted = getAdventureSprayingPermitted(pos, level, stack, player, activeLayer);

        final boolean opped = player != null && isOpped(player);

        if (StreetArt.AREA_LIB.isInRegion(level, pos, AreaLib.Type.PROTECTED)) {
            if (!adventurePermitted || !opped) {
                return false;
            }
        }

        return adventurePermitted;
    }

    public static boolean splashingAllowed(final BlockPos pos, final ServerLevel level, @Nullable final Player player) {
        final boolean adventurePermitted = getAdventureSplashingAllowed(pos, level, player);

        final boolean opped = player != null && isOpped(player);

        if (StreetArt.AREA_LIB.isInRegion(level, pos, AreaLib.Type.PROTECTED)) {
            if (!adventurePermitted || !opped) {
                return false;
            }
        }

        return adventurePermitted;
    }

    public static boolean getAdventureSprayingPermitted(final BlockPos pos, final Level level, final ItemStack stack, @Nullable final Player player, final Identifier activeLayer) {
        /*if (activeLayer.equals(AllGraffitiLayers.GLASSES_LAYER.identifier())) {
            if (ClientBoundGameRuleSync.get(level).adventureSecondLayerPainting()) {
                return true;
            }
        } else {*/
            if (ClientBoundGameRuleSync.get(level).adventurePainting()) {
                return true;
            }
//        }

        if (player == null) {
            if (level instanceof final ServerLevel serverLevel) {
                if (!serverLevel.getGameRules().get(AllGameRules.NON_PLAYERS_ADVENTURE)) {
                    return true;
                }
            }
        }

        if (player == null || player.gameMode() == null || player.gameMode().isBlockPlacingRestricted()) {
            return canPlaceOn(pos, level, stack) || StreetArt.AREA_LIB.isInRegion(level, pos, AreaLib.Type.SPRAYING_ALLOWED);
        }

        return true;
    }

    public static boolean getAdventureSplashingAllowed(final BlockPos pos, final ServerLevel level, @Nullable final Player player) {
        if (player == null) {
            if (!level.getGameRules().get(AllGameRules.NON_PLAYERS_ADVENTURE)) {
                return true;
            }
        }

        if (level.getGameRules().get(AllGameRules.ADVENTURE_SPLASHING)) {
            return true;
        }

        if (player == null || player.gameMode() == null || player.gameMode().isBlockPlacingRestricted()) {
            return StreetArt.AREA_LIB.isInRegion(level, pos, AreaLib.Type.SPLASHES_ALLOWED);
        }

        return true;
    }

    public static boolean canPlaceOn(final BlockPos pos, final Level level, final ItemStack stack) {
        final AdventureModePredicate predicate = stack.get(DataComponents.CAN_PLACE_ON);
        return predicate != null && predicate.test(new BlockInWorld(level, pos, false));
    }
}
