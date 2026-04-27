package com.streetart.client;

import com.streetart.AllEntityTypes;
import com.streetart.StreetArt;
import com.streetart.client.manager.SpraySessionManager;
import com.streetart.client.rendering.GraffitiAtlasLayers;
import com.streetart.client.rendering.GraffitiRenderer;
import com.streetart.client.rendering.TrackRenderer;
import com.streetart.client.rendering.rollerblades.RollerbladeRenderer;
import com.streetart.networking.ClientBoundGameRuleSync;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class StreetArtClient implements ClientModInitializer {

    public static GraffitiAtlasLayers layers;

    @Override
    public void onInitializeClient() {
        LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(context -> {
            GraffitiRenderer.render(context);
            TrackRenderer.render(context);
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(
                _ -> {
                    layers = new GraffitiAtlasLayers(Minecraft.getInstance().getTextureManager());
                    layers.registerPacketsAndEvents();
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(ClientBoundGameRuleSync.TYPE, (p, c) -> {
            ClientBoundGameRuleSync.CLIENT_CURRENT = p;
        });

        ClientTickEvents.END_CLIENT_TICK.register((l) -> {
            SpraySessionManager.tick(l);
            StreetArt.recordingManager.tick(l.player, l.level);
        });

        EntityRenderers.register(AllEntityTypes.PAINT_BALLOON, ThrownItemRenderer::new);

        new RollerbladeRenderer().init();
        AllDataComponentProperties.init();
        AllTintSources.init();

        if (FabricLoader.getInstance().isModLoaded("area_lib")) {
            ClientAreaLibStuff.init();
        }
    }
}