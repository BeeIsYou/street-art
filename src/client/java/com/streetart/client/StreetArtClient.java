package com.streetart.client;

import com.streetart.client.manager.GClientManager;
import com.streetart.client.manager.SpraySessionManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;

public class StreetArtClient implements ClientModInitializer {
	public static GClientManager textureManager;

	@Override
	public void onInitializeClient() {
		LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(GraffitiRenderer::render);

		ClientLifecycleEvents.CLIENT_STARTED.register(
			_ -> {
				StreetArtClient.textureManager = new GClientManager(Minecraft.getInstance().getTextureManager());
			}
		);

		ClientTickEvents.END_CLIENT_TICK.register(SpraySessionManager::tick);

		// todo find clientside level unload event :p
		ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> StreetArtClient.textureManager.closeAll());
	}
}