package com.streetart.managers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class GraffitiGlobalManager {

    private static final Map<Level, GraffitiLevelManager> LEVEL_GRAFFITI_MAP = new HashMap<>();


    public static GraffitiLevelManager getGraffitiLevelManager(final ServerLevel level) {
        return LEVEL_GRAFFITI_MAP.computeIfAbsent(level, l -> new GraffitiLevelManager(level));
    }

    public static void tickLevel(final ServerLevel level) {
        final GraffitiLevelManager manager = LEVEL_GRAFFITI_MAP.get(level);
        if (manager != null) {
            manager.tick();
        }
    }




}
