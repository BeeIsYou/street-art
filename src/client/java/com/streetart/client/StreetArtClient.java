package com.streetart.client;

import com.streetart.networking.ClientBoundGraffitUpdate;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;

public class StreetArtClient implements ClientModInitializer {
	public static GraffitiManager textureManager;

	@Override
	public void onInitializeClient() {
		LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(GraffitiRenderer::render);

		ClientLifecycleEvents.CLIENT_STARTED.register(
			_ -> StreetArtClient.textureManager = new GraffitiManager(Minecraft.getInstance().getTextureManager())
		);

		// todo find clientside level unload event :p
		ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> StreetArtClient.textureManager.closeAll());

		ClientPlayNetworking.registerGlobalReceiver(ClientBoundGraffitUpdate.TYPE, (packet, ctx) -> {
			textureManager.getOrNew(packet.pos(), packet.depth(), packet.dir());
		});
	}
}