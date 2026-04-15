package com.streetart;

import com.streetart.arealib.AreaLib;
import com.streetart.arealib.AreaLibPresent;
import com.streetart.managers.GraffitiGlobalManager;
import com.streetart.networking.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreetArt implements ModInitializer {
    public static final String MOD_ID = "street_art";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static AreaLib AREA_LIB;

    public static Identifier id(final String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        AllDataComponents.init();
        AllItems.init();
        AllEntityTypes.init();
        AllGameRules.init();
        AttachmentTypes.init();
        AllCommands.init();

        PayloadTypeRegistry.clientboundPlay().register(ClientBoundGraffitiSet.TYPE, ClientBoundGraffitiSet.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundInvalidateBlock.TYPE, ClientBoundInvalidateBlock.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BiDirectionalGraffitiChange.TYPE, BiDirectionalGraffitiChange.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundGameRuleSync.TYPE, ClientBoundGameRuleSync.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(ServerBoundRequestDataPacket.TYPE, ServerBoundRequestDataPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BiDirectionalGraffitiChange.TYPE, BiDirectionalGraffitiChange.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerBoundRequestDataPacket.TYPE, GraffitiGlobalManager::handleRequestPacket);

        ServerPlayNetworking.registerGlobalReceiver(BiDirectionalGraffitiChange.TYPE, GraffitiGlobalManager::handleChange);

        ServerPlayerEvents.JOIN.register(ClientBoundGameRuleSync::onJoin);

        CommandRegistrationCallback.EVENT.register(AllCommands::register);

        if (FabricLoader.getInstance().isModLoaded("area_lib")) {
            AREA_LIB = new AreaLibPresent();
        } else {
            AREA_LIB = new AreaLib();
        }
    }
}