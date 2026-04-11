package com.streetart.client.manager;

import com.streetart.GManager;
import com.streetart.networking.ClientBoundGraffitUpdate;
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
    }

    public int nextID() {
        return this.id++;
    }

    @Override
    public GClientBlock newBlockData(BlockPos pos) {
        return new GClientBlock(pos);
    }

    @Override
    protected Map<BlockPos, GClientBlock> getGraffiti() {
        return this.graffiti;
    }

    public void forEach(Consumer<GClientData> consumer) {
        for (Map.Entry<BlockPos, GClientBlock> graffitis : this.getGraffiti().entrySet()) {
            BlockPos pos = graffitis.getKey();
            for (Map.Entry<Direction, List<GClientData>> tiles : graffitis.getValue().entrySet()) {
                for (GClientData tile : tiles.getValue()) {
                    consumer.accept(tile);
                }
            }
        }
    }

    public void handlePacket(ClientBoundGraffitUpdate packet, ClientPlayNetworking.Context context) {
        GClientData data = this.getOrCreate(packet.pos(), packet.dir(), packet.depth());
        data.update(packet.textureData());
        data.updateLight(context.client().level);
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
