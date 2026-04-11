package com.streetart.client.manager;

import com.streetart.GManager;
import com.streetart.networking.ClientBoundGraffitUpdate;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector3d;

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
    public GClientBlock newBlockData() {
        return new GClientBlock();
    }

    @Override
    protected Map<BlockPos, GClientBlock> getGraffiti() {
        return this.graffiti;
    }

    public void forEach(Consumer<TileData> consumer) {
        TileData data = new TileData();
        for (Map.Entry<BlockPos, GClientBlock> graffitis : this.getGraffiti().entrySet()) {
            BlockPos pos = graffitis.getKey();
            for (Map.Entry<Direction, List<GClientData>> tiles : graffitis.getValue().entrySet()) {
                data.dir = tiles.getKey();
                for (GClientData tile : tiles.getValue()) {
                    data.pos.set(pos.getX(), pos.getY(), pos.getZ());
                    data.pos.fma(-tile.depth + 0.01, data.dir.getUnitVec3f());
                    data.tile = tile;
                    consumer.accept(data);
                }
            }
        }
    }

    public void handlePacket(ClientBoundGraffitUpdate packet, ClientPlayNetworking.Context context) {
        this.getOrCreate(packet.pos(), packet.dir(), packet.depth()).update(packet.textureData());
    }

    public void updateLights(ClientLevel level) {
        this.forEach(data -> {
            data.tile.light = LevelRenderer.getLightCoords(level, BlockPos.containing(data.pos.x, data.pos.y, data.pos.z));
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

    public static class TileData {
        public Vector3d pos = new Vector3d();
        public Direction dir;
        public GClientData tile;
    }
}
