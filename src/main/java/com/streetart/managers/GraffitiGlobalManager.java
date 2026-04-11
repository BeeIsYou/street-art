package com.streetart.managers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class GraffitiGlobalManager {

    private static final Map<Level, GLevelManager> LEVEL_GRAFFITI_MAP = new HashMap<>();


    public static GLevelManager getGraffitiLevelManager(final ServerLevel level) {
        return LEVEL_GRAFFITI_MAP.computeIfAbsent(level, l -> new GLevelManager(level));
    }

    public static void tickLevel(final ServerLevel level) {
        final GLevelManager manager = LEVEL_GRAFFITI_MAP.get(level);
        if (manager != null) {
            manager.tick();
        }
    }




}
