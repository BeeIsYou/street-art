package com.streetart;

import com.streetart.managers.GraffitiGlobalManager;
import com.streetart.networking.ClientBoundGraffitUpdate;
import com.streetart.networking.ClientBoundInvalidateBlock;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreetArt implements ModInitializer {
	public static final String MOD_ID = "street_art";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		AllItems.init();

		ServerTickEvents.END_LEVEL_TICK.register(GraffitiGlobalManager::tickLevel);
		PayloadTypeRegistry.clientboundPlay().register(ClientBoundGraffitUpdate.TYPE, ClientBoundGraffitUpdate.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ClientBoundInvalidateBlock.TYPE, ClientBoundInvalidateBlock.CODEC);
	}
}