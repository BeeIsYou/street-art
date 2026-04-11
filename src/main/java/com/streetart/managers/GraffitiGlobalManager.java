package com.streetart.managers;

import com.streetart.networking.ServerBoundGraffitiUpdate;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

    public static void handleServerUpdatePacket(ServerBoundGraffitiUpdate packet, ServerPlayNetworking.Context context) {
        for (byte b : packet.textureData()) {
            if (b != 0) {
                // todo length validation
                GServerData data = getGraffitiLevelManager(context.player().level()).getOrCreate(packet.pos(), packet.dir(), packet.depth());
                System.arraycopy(packet.textureData(), 0, data.graffitiData, 0, packet.textureData().length);
                return;
            }
        }
        // todo mark specific thing removed
    }
}
