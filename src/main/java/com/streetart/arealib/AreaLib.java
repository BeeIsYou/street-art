package com.streetart.arealib;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class AreaLib {
    public enum Type {
        /** Adventure players can always modify with spray cans and pressure washers */
        SPRAYING_ALLOWED("modifying_allowed"),
        /** Adventure players can always modify with paint balloons */
        SPLASHES_ALLOWED("paint_balloon_allowed"),
        /** Only opped players can modify */
        PROTECTED("protected"),
        /** Will not naturally disappear over time */
        NO_DECAY("no_decay");

        public final String id;
        Type(final String id) {
            this.id = id;
        }
    }

    public boolean isLoaded() {
        return false;
    }

    public boolean isInRegion(final Level level, final BlockPos pos, final Type type) {
        return false;
    }

    public void createRegion(final ServerLevel level, final Type type, final BlockPos a, final BlockPos b) {}
    public void removeRegion(final ServerLevel level, final Type type, final BlockPos pos) {}
}
