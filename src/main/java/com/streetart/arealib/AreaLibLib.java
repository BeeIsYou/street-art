package com.streetart.arealib;

import com.streetart.StreetArt;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.component.SampledAreaComponentType;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.registry.AreaComponentRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AreaLibLib extends AreaLiblessLib {
    // adventure players can modify
    public static final SampledAreaComponentType<Unit> MODIFYING_ALLOWED =
            AreaComponentRegistry.registerSampled(StreetArt.id("modifying_allowed"), Unit.CODEC);

    // deopped players can not modify. also will never decay over time
    public static final SampledAreaComponentType<Unit> PROTECTED =
            AreaComponentRegistry.registerSampled(StreetArt.id("protected"), Unit.CODEC);

    @Override
    public boolean allowedToEdit(Player player, BlockPos pos) {
        AreaSavedData data = AreaLib.getSavedData(player.level());

        Vec3 center = Vec3.atCenterOf(pos);

        boolean adventurePermits = adventurePermits(player, pos);
        boolean opped = Commands.LEVEL_ADMINS.check(player.permissions());
        boolean restricted = data.isInSampledAreaWith(PROTECTED, player.level(), center);
        boolean permitted = data.isInSampledAreaWith(MODIFYING_ALLOWED, player.level(), center);

        if (adventurePermits && !restricted) {
            return true;
        }

        if (permitted && !restricted) {
            return true;
        }

        if (opped && adventurePermits) {
            return true;
        }

        return false; // is adventure or in a restricted zone (and other permissions don't let you do that)
    }

    @Override
    public boolean decays(Level level, BlockPos pos) {
        AreaSavedData data = AreaLib.getSavedData(level);
        boolean restricted = data.isInSampledAreaWith(PROTECTED, level, Vec3.atCenterOf(pos));
        return !restricted;
    }
}
