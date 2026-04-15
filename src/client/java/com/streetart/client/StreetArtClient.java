package com.streetart.client;

import com.streetart.AllEntityTypes;
import com.streetart.client.manager.GClientManager;
import com.streetart.client.manager.SpraySessionManager;
import com.streetart.client.rendering.GraffitiRenderer;
import com.streetart.client.rendering.TileAtlasManager;
import com.streetart.graffiti_data.TileChange;
import com.streetart.graffiti_data.TileKey;
import com.streetart.networking.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public class StreetArtClient implements ClientModInitializer {

    public static Map<ChunkPos, GClientManager> textureManager;

    public static TileAtlasManager tileAtlasManager;

    @Override
    public void onInitializeClient() {
        LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(GraffitiRenderer::render);

        ClientLifecycleEvents.CLIENT_STARTED.register(
                _ -> {
                    textureManager = new HashMap<>();
                    tileAtlasManager = new TileAtlasManager(Minecraft.getInstance().getTextureManager());

                    ClientPlayNetworking.registerGlobalReceiver(ClientBoundGraffitiSet.TYPE, (l, ll) -> {
                        final GClientManager man = textureManager.computeIfAbsent(ChunkPos.containing(l.pos()), _ -> new GClientManager());
                        man.handleDataUpdate(l, ll);
                    });

                    ClientPlayNetworking.registerGlobalReceiver(ClientBoundInvalidateBlock.TYPE, (l, ll) -> {
                        final GClientManager manager = textureManager.computeIfAbsent(ChunkPos.containing(l.pos()), _ -> new GClientManager());
                        manager.handleBlockInvalidate(l, ll);
                    });

                    ClientPlayNetworking.registerGlobalReceiver(BiDirectionalGraffitiChange.TYPE, (l, ll) -> {
                        for (final Map.Entry<TileKey, TileChange> entries : l.changes().entrySet()) {
                            final GClientManager manager = textureManager.computeIfAbsent(ChunkPos.containing(entries.getKey().pos()), _ -> new GClientManager());
                            manager.handleChange(l, ll);
                        }
                    });

                    ClientTickEvents.END_CLIENT_TICK.register((m) -> StreetArtClient.textureManager.values().forEach(man -> man.tick(m)));
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(ClientBoundGameRuleSync.TYPE, (p, c) -> {
            ClientBoundGameRuleSync.CLIENT_CURRENT = p;
        });

        ClientTickEvents.END_CLIENT_TICK.register(SpraySessionManager::tick);
        ClientTickEvents.END_CLIENT_TICK.register((l) -> {
            if (l.level == null) {
                textureManager.entrySet().removeIf((e) -> {
                    e.getValue().closeAll();
                    return true;
                });
            }
        });

        ClientChunkEvents.CHUNK_UNLOAD.register((l, ll) -> {
            final GClientManager manager = textureManager.remove(ll.getPos());
            if (manager != null) {
                manager.closeAll();
            }
        });

        ClientChunkEvents.CHUNK_LOAD.register((l, ll) -> {
            ClientPlayNetworking.send(new ServerBoundRequestDataPacket(ll.getPos()));
        });

        EntityRenderers.register(AllEntityTypes.PAINT_BALLOON, ThrownItemRenderer::new);

        if (FabricLoader.getInstance().isModLoaded("area_lib")) {
            ClientAreaLibStuff.init();
        }
    }
}