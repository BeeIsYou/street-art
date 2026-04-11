package com.streetart.client.manager;

import com.streetart.GManager;
import com.streetart.networking.ClientBoundGraffitUpdate;
import com.streetart.networking.ClientBoundInvalidateBlock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GClientManager extends GManager<GClientData, GClientBlock, GClientManager> {
    private Map<BlockPos, GClientBlock> graffiti = new HashMap<>();
    public final TextureManager textureManager;
    private int id = 0;

    public GClientManager(TextureManager textureManager) {
        this.textureManager = textureManager;

        // how many people will put me down for doing THIS
        ClientPlayNetworking.registerGlobalReceiver(ClientBoundGraffitUpdate.TYPE, this::handleDataUpdate);
        ClientPlayNetworking.registerGlobalReceiver(ClientBoundInvalidateBlock.TYPE, this::handleBlockInvalidate);
        ClientTickEvents.END_LEVEL_TICK.register(this::updateLights);
    }

    public int nextID() {
        return this.id++;
    }

    @Override
    public GClientBlock newBlockData(BlockPos pos) {
        return new GClientBlock(pos);
    }

    @Override
    public Map<BlockPos, GClientBlock> getGraffiti() {
        return this.graffiti;
    }

    public void forEach(Consumer<GClientData> consumer) {
        for (GClientBlock graffitis : this.getGraffiti().values()) {
            for (Map.Entry<Direction, List<GClientData>> tiles : graffitis.getBlockData().entrySet()) {
                for (GClientData tile : tiles.getValue()) {
                    consumer.accept(tile);
                }
            }
        }
    }

    public void handleDataUpdate(ClientBoundGraffitUpdate packet, ClientPlayNetworking.Context context) {
        if (packet.textureData().length == 16*16*4) {
            GClientData data = this.getOrCreate(packet.pos(), packet.dir(), packet.depth());
            data.update(packet.textureData());
            data.updateLight(context.client().level);
        } else {
            GClientBlock block = this.getGraffiti().get(packet.pos());
            if (block != null) {
                List<GClientData> dataList = block.getBlockData().get(packet.dir());
                if (dataList != null) {
                    dataList.removeIf(d -> {
                        boolean remove = d.depth == packet.depth();
                        if (remove) {
                            d.close();
                        }
                        return remove;
                    });
                }
            }
        }
    }

    public void handleBlockInvalidate(ClientBoundInvalidateBlock packet, ClientPlayNetworking.Context context) {
        GClientBlock block = this.getGraffiti().remove(packet.pos());
        if (block != null) {
            block.close();
        }
    }

    public void updateLights(ClientLevel level) {
        this.forEach(data -> {
            data.updateLight(level);
        });
    }

    public void closeAll() {
        for (GClientBlock graffitis : this.getGraffiti().values()) {
            graffitis.close();
        }
        this.graffiti.clear();
    }

    @Override
    public void close() {
        this.closeAll();
    }
}
