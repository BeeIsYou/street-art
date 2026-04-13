package com.streetart.arealib;

import com.streetart.StreetArt;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.areas.BoxArea;
import dev.doublekekse.area_lib.component.SampledAreaComponentType;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.registry.AreaComponentRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AreaLibLib extends AreaLiblessLib {
    public static SampledAreaComponentType<Unit> getComponent(final AreaType type) {
        return switch (type) {
            case MODIFYING_ALLOWED -> MODIFYING_ALLOWED;
            case PROTECTED -> PROTECTED;
            case NO_DECAY -> NO_DECAY;
        };
    }

    @Override
    public void init() {

    }

    // adventure players can modify
    public static final SampledAreaComponentType<Unit> MODIFYING_ALLOWED =
            AreaComponentRegistry.registerSampled(StreetArt.id("modifying_allowed"), Unit.CODEC);

    // deopped players can not modify
    public static final SampledAreaComponentType<Unit> PROTECTED =
            AreaComponentRegistry.registerSampled(StreetArt.id("protected"), Unit.CODEC);

    // spray paint will not disappear over time
    public static final SampledAreaComponentType<Unit> NO_DECAY =
            AreaComponentRegistry.registerSampled(StreetArt.id("no_decay"), Unit.CODEC);

    @Override
    public boolean allowedToEdit(final Player player, final BlockPos pos) {
        final AreaSavedData data = AreaLib.getSavedData(player.level());

        final Vec3 center = Vec3.atCenterOf(pos);

        final boolean adventurePermits = adventurePermits(player, pos);
        final boolean opped = Commands.LEVEL_ADMINS.check(player.permissions());
        final boolean restricted = data.isInSampledAreaWith(PROTECTED, player.level(), center);
        final boolean permitted = data.isInSampledAreaWith(MODIFYING_ALLOWED, player.level(), center);

        if (adventurePermits && !restricted) {
            return true;
        }

        if (permitted && !restricted) {
            return true;
        }

        return opped && adventurePermits;// is adventure or in a restricted zone (and other permissions don't let you do that)
    }

    @Override
    public boolean decays(final Level level, final BlockPos pos) {
        final AreaSavedData data = AreaLib.getSavedData(level);
        final boolean restricted = data.isInSampledAreaWith(NO_DECAY, level, Vec3.atCenterOf(pos));
        return !restricted;
    }

    public void createRegion(final ServerLevel level, final AreaType type, final BlockPos a, final BlockPos b) {
        final AreaSavedData data = AreaLib.getSavedData(level);
        final BoxArea area = new BoxArea(
                data,
                StreetArt.id(String.format("generated/%s/%d_%d_%d/%d_%d_%d",
                        type.id,
                        a.getX(), a.getY(), a.getZ(),
                        b.getX(), b.getY(), b.getZ()
                )),
                level.dimension().identifier(),
                new AABB(a).minmax(new AABB(b))
        );
        area.put(level.getServer(), getComponent(type), Unit.INSTANCE);
        AreaLib.getSavedData(level).put(level.getServer(), area);
    }

    public void removeRegion(final ServerLevel level, final AreaType type, final BlockPos pos) {
        final AreaSavedData data = AreaLib.getSavedData(level);
        for (final Area area : AreaLib.getSavedData(level).findAllAreasContaining(level, Vec3.atCenterOf(pos))) {
            if (area.has(getComponent(type))) {
                data.remove(level.getServer(), area);
                return;
            }
        }
    }
}
