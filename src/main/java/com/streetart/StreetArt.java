package com.streetart;

import com.streetart.arealib.AreaLibLib;
import com.streetart.arealib.AreaLiblessLib;
import com.streetart.managers.GraffitiGlobalManager;
import com.streetart.networking.ClientBoundGraffitiUpdate;
import com.streetart.networking.ClientBoundInvalidateBlock;
import com.streetart.networking.ServerBoundGraffitiUpdate;
import com.streetart.networking.ServerBoundRequestDataPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreetArt implements ModInitializer {
    public static final String MOD_ID = "street_art";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static AreaLiblessLib AREA_LIB;

    public static Identifier id(final String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        AllDataComponents.init();
        AllItems.init();
        AttachmentTypes.init();

        PayloadTypeRegistry.clientboundPlay().register(ClientBoundGraffitiUpdate.TYPE, ClientBoundGraffitiUpdate.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundInvalidateBlock.TYPE, ClientBoundInvalidateBlock.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(ServerBoundRequestDataPacket.TYPE, ServerBoundRequestDataPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundGraffitiUpdate.TYPE, ServerBoundGraffitiUpdate.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerBoundGraffitiUpdate.TYPE, GraffitiGlobalManager::handleServerUpdatePacket);
        ServerPlayNetworking.registerGlobalReceiver(ServerBoundRequestDataPacket.TYPE, GraffitiGlobalManager::handleRequestPacket);

        if (FabricLoader.getInstance().isModLoaded("area_lib")) {
            AREA_LIB = new AreaLibLib();
        } else {
            AREA_LIB = new AreaLiblessLib();
        }
    }
}