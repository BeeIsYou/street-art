package com.streetart.client.rendering;

import com.streetart.AllGraffitiLayers;
import com.streetart.graffiti_data.GraffitiLayerType;
import com.streetart.networking.BiDirectionalGraffitiChange;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ClientBoundInvalidateBlock;
import com.streetart.networking.ServerBoundRequestDataPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class GraffitiAtlasLayers {
    private final Map<Identifier, GraffitiAtlas> atlasLayers = new HashMap<>();

    public GraffitiAtlasLayers(final TextureManager textureManager) {
        for (final GraffitiLayerType graffitiLayerType : AllGraffitiLayers.LAYER_REGISTRY) {
            final Identifier identifier = AllGraffitiLayers.LAYER_REGISTRY.getKey(graffitiLayerType);
            assert identifier != null;
            this.atlasLayers.put(identifier, new GraffitiAtlas(
                    textureManager, identifier, 4
            ));
        }
    }

    public GraffitiAtlas get(final Identifier layer) {
        return this.atlasLayers.get(layer);
    }

    public void forEach(final BiConsumer<Identifier, GraffitiAtlas> consumer) {
        this.atlasLayers.forEach(consumer);
    }

    public GraffitiAtlas active() {
        final GraffitiLayerType layer = AllGraffitiLayers.getActive(Minecraft.getInstance().player, Minecraft.getInstance().level);
        assert layer != null;
        return this.atlasLayers.get(layer.identifier());
    }

    public void closeAll() {
        this.atlasLayers.forEach((_, atlas) -> {
            atlas.clear();
        });
    }

    public void registerPacketsAndEvents() {
        ClientPlayNetworking.registerGlobalReceiver(ClientBoundGraffitiSet.TYPE, (packet, context) -> {
            if (packet.layer().isEmpty()) { // shouldn't happen
                return;
            }
            this.get(packet.layer().get()).handleSetPacket(packet, context);
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientBoundInvalidateBlock.TYPE, (packet, context) -> {
            if (packet.layer().isEmpty()) {
                this.atlasLayers.forEach((_, atlas) -> atlas.handleInvalidatePacket(packet, context));
            } else {
                this.atlasLayers.get(packet.layer().get()).handleInvalidatePacket(packet, context);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(BiDirectionalGraffitiChange.TYPE, (packet, context) -> {
            this.atlasLayers.get(packet.layer()).handleChangePacket(packet, context);
        });

        ClientChunkEvents.CHUNK_LOAD.register((_, chunk) -> {
            ClientPlayNetworking.send(new ServerBoundRequestDataPacket(chunk.getPos()));
        });

        ClientTickEvents.END_CLIENT_TICK.register((minecraft) -> {
            for (final GraffitiAtlas atlas : this.atlasLayers.values()) {
                atlas.checkClear(minecraft);
            }
        });

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((l, ll) -> {
            for (final GraffitiAtlas atlas : this.atlasLayers.values()) {
                atlas.closeAll();
            }
        });

        ClientChunkEvents.CHUNK_UNLOAD.register((_, chunk) -> {
            for (final GraffitiAtlas atlas : this.atlasLayers.values()) {
                atlas.handleChunkUnload(chunk);
            }
        });
    }
}
