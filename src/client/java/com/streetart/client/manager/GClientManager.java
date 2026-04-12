package com.streetart.client.manager;

import com.streetart.ArtUtil;
import com.streetart.GManager;
import com.streetart.client.texture.TileAtlasManager;
import com.streetart.networking.ClientBoundGraffitiSet;
import com.streetart.networking.ClientBoundInvalidateBlock;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GClientManager extends GManager<GClientData, GClientBlock> {
    private Map<BlockPos, GClientBlock> graffiti = new HashMap<>();
    public final TileAtlasManager tileAtlasManager;

    private int syncTimer = 0;

    public GClientManager(TextureManager textureManager) {
        this.tileAtlasManager = new TileAtlasManager(textureManager);

        // how many people will put me down for doing THIS
    }

    public int nextID() {
        return this.tileAtlasManager.allocateID();
    }

    @Override
    public GClientBlock newBlockData(BlockPos pos) {
        return new GClientBlock(pos);
    }

    @Override
    public Map<BlockPos, GClientBlock> getGraffiti() {
        return this.graffiti;
    }

    public void applyPixelChange(BlockHitResult hitResult, Vector2i coordinates, int color) {
        this.getOrCreate(
                hitResult.getBlockPos(),
                hitResult.getDirection(),
                ArtUtil.calculateDepth(hitResult)
        ).applyPixel(coordinates, color);
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

    public void tick(Minecraft minecraft) {
        if (minecraft.getConnection() != null) {
            this.syncTimer++;
            if (this.syncTimer > 10) {
                SpraySessionManager.sync();
                this.syncTimer = 0;
            }
        }
        this.tileAtlasManager.checkDirty();
    }

    public void handleDataUpdate(ClientBoundGraffitiSet packet, ClientPlayNetworking.Context context) {
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
                        boolean remove = d.getDepth() == packet.depth();
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
            block.spawnParticles(context.client().level);
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
