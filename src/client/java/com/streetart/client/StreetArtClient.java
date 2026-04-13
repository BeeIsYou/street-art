package com.streetart.client;

import com.streetart.AllEntityTypes;
import com.streetart.StreetArt;
import com.streetart.client.manager.GClientManager;
import com.streetart.client.manager.SpraySessionManager;
import com.streetart.client.texture.GraffitiRenderer;
import com.streetart.networking.BiDirectionalGraffitiChange;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ClientBoundInvalidateBlock;
import com.streetart.networking.ServerBoundRequestDataPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class StreetArtClient implements ClientModInitializer {
	public static GClientManager textureManager;

	@Override
	public void onInitializeClient() {
		LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(GraffitiRenderer::render);

		ClientLifecycleEvents.CLIENT_STARTED.register(
			_ -> {
				StreetArtClient.textureManager = new GClientManager(Minecraft.getInstance().getTextureManager());

				ClientPlayNetworking.registerGlobalReceiver(ClientBoundGraffitiSet.TYPE, StreetArtClient.textureManager::handleDataUpdate);
				ClientPlayNetworking.registerGlobalReceiver(ClientBoundInvalidateBlock.TYPE, StreetArtClient.textureManager::handleBlockInvalidate);
				ClientPlayNetworking.registerGlobalReceiver(BiDirectionalGraffitiChange.TYPE, StreetArtClient.textureManager::handleChange);

				ClientTickEvents.END_LEVEL_TICK.register(StreetArtClient.textureManager::updateLights);
				ClientTickEvents.END_CLIENT_TICK.register(StreetArtClient.textureManager::tick);
			}
		);

		ClientTickEvents.END_CLIENT_TICK.register(SpraySessionManager::tick);

		ClientChunkEvents.CHUNK_LOAD.register((l, ll) -> {
			ClientPlayNetworking.send(new ServerBoundRequestDataPacket(ll.getPos()));
		});

		// todo find clientside world leave event grrrrrrrrr
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((_, _) -> StreetArtClient.textureManager.closeAll());

		EntityRenderers.register(AllEntityTypes.PAINT_BALLOON, ThrownItemRenderer::new);

		ItemTintSources.ID_MAPPER.put(StreetArt.id("color"), ColorComponentTint.MAP_CODEC);

		if (FabricLoader.getInstance().isModLoaded("area_lib")) {
			ClientAreaLibStuff.init();
		}
	}
}