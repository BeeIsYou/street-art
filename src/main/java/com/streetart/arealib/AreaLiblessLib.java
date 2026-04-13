package com.streetart.arealib;

import com.streetart.item.SprayPaintInteractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class AreaLiblessLib {
    public enum AreaType {
        MODIFYING_ALLOWED("modifying_allowed"),
        PROTECTED("protected"),
        NO_DECAY("no_decay");

        public final String id;
        AreaType(final String id) {
            this.id = id;
        }
    }

    public void init() {}

    public boolean allowedToEdit(final Player player, final BlockPos pos) {
        return adventurePermits(player, pos);
    }

    public boolean decays(final Level level, final BlockPos pos) {
        return true;
    }

    public static boolean adventurePermits(final Player player, final BlockPos pos) {
        if (!player.gameMode().isBlockPlacingRestricted()) {
            return true;
        }

        final BlockInWorld block = new BlockInWorld(player.level(), pos, false);
        final ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof SprayPaintInteractor && permitsUsage(main, block)) {
            return true;
        }

        final ItemStack offHand = player.getMainHandItem();
        return offHand.getItem() instanceof SprayPaintInteractor && permitsUsage(offHand, block);
    }

    public static boolean permitsUsage(final ItemStack stack, final BlockInWorld block) {
        final AdventureModePredicate predicate = stack.get(DataComponents.CAN_PLACE_ON);
        return predicate != null && predicate.test(block);
    }

    public void createRegion(final Level level, final MinecraftServer server, final AreaType type, final BlockPos a, final BlockPos b) {}
}
