package com.streetart.arealib;

import com.streetart.StreetArt;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.areas.BoxArea;
import dev.doublekekse.area_lib.component.SampledAreaComponentType;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.registry.AreaComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AreaLibPresent extends AreaLib {
    public static SampledAreaComponentType<Unit> getComponent(final Type type) {
        return switch (type) {
            case SPRAYING_ALLOWED -> SPRAYING_ALLOWED;
            case SPLASHES_ALLOWED -> SPLASHING_ALLOWED;
            case PROTECTED -> PROTECTED;
            case NO_DECAY -> NO_DECAY;
        };
    }

    public static final SampledAreaComponentType<Unit> SPRAYING_ALLOWED =
            AreaComponentRegistry.registerSampled(StreetArt.id("spraying_allowed"), Unit.CODEC);

    public static final SampledAreaComponentType<Unit> SPLASHING_ALLOWED =
            AreaComponentRegistry.registerSampled(StreetArt.id("splashing_allowed"), Unit.CODEC);

    public static final SampledAreaComponentType<Unit> PROTECTED =
            AreaComponentRegistry.registerSampled(StreetArt.id("protected"), Unit.CODEC);

    public static final SampledAreaComponentType<Unit> NO_DECAY =
            AreaComponentRegistry.registerSampled(StreetArt.id("no_decay"), Unit.CODEC);

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public SavedData getSavedData(final Level level) {
        return new SavedDataPresent(dev.doublekekse.area_lib.AreaLib.getSavedData(level));
    }

    @Override
    public boolean isInRegion(final Level level, final BlockPos pos, final Type type) {
        final AreaSavedData data = dev.doublekekse.area_lib.AreaLib.getSavedData(level);
        return data.isInSampledAreaWith(getComponent(type), level, Vec3.atCenterOf(pos));
    }

    @Override
    public boolean isInRegion(final SavedData data, final Level level, final BlockPos pos, final Type type) {
        // a bit hacky but whatever
        return ((SavedDataPresent)data).wrapped.isInSampledAreaWith(getComponent(type), level, Vec3.atCenterOf(pos));
    }

    public void createRegion(final ServerLevel level, final Type type, final BlockPos a, final BlockPos b) {
        final AreaSavedData data = dev.doublekekse.area_lib.AreaLib.getSavedData(level);
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
        dev.doublekekse.area_lib.AreaLib.getSavedData(level).put(level.getServer(), area);
    }

    public void removeRegion(final ServerLevel level, final Type type, final BlockPos pos) {
        final AreaSavedData data = dev.doublekekse.area_lib.AreaLib.getSavedData(level);
        for (final Area area : dev.doublekekse.area_lib.AreaLib.getSavedData(level).findAllAreasContaining(level, Vec3.atCenterOf(pos))) {
            if (area.has(getComponent(type))) {
                data.remove(level.getServer(), area);
                return;
            }
        }
    }

    public record SavedDataPresent(AreaSavedData wrapped) implements SavedData { }
}
