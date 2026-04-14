package com.streetart.client.manager;

import com.streetart.ArtUtil;
import com.streetart.GManager;
import com.streetart.client.texture.TileAtlasManager;
import com.streetart.component.ColorComponent;
import com.streetart.graffiti_data.TileChange;
import com.streetart.graffiti_data.TileKey;
import com.streetart.networking.BiDirectionalGraffitiChange;
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
    private final Map<BlockPos, GClientBlock> graffiti = new HashMap<>();
    public final TileAtlasManager tileAtlasManager;

    private final int syncTimer = 0;

    public GClientManager(final TextureManager textureManager) {
        this.tileAtlasManager = new TileAtlasManager(textureManager);

        // how many people will put me down for doing THIS
    }

    public int nextID() {
        return this.tileAtlasManager.allocateID();
    }

    @Override
    public GClientBlock newBlockData(final BlockPos pos) {
        return new GClientBlock(pos);
    }

    @Override
    public Map<BlockPos, GClientBlock> getGraffiti() {
        return this.graffiti;
    }

    /**
     * Will not create new data if color == 0
     * @return true if pixel changed
     */
    public boolean applyPixelChange(final BlockHitResult hitResult, final Vector2i coordinates, final int color) {
        final GClientData data = this.getOrConditionalCreate(hitResult.getBlockPos(),
                hitResult.getDirection(),
                ArtUtil.calculateDepth(hitResult),
                color == 0
        );
        if (data == null) {
            return false;
        }
        return data.applyPixel(coordinates, color);
    }

    public void forEach(final Consumer<GClientData> consumer) {
        for (final GClientBlock graffitis : this.getGraffiti().values()) {
            for (final Map.Entry<Direction, List<GClientData>> tiles : graffitis.getBlockData().entrySet()) {
                for (final GClientData tile : tiles.getValue()) {
                    consumer.accept(tile);
                }
            }
        }
    }

    public void tick(final Minecraft minecraft) {
        if (minecraft.getConnection() != null) {
//            this.syncTimer++;
//            if (this.syncTimer > 10) {
                SpraySessionManager.sync();
//                this.syncTimer = 0;
//            }
        }
        this.tileAtlasManager.checkDirty();
    }

    public void handleDataUpdate(final ClientBoundGraffitiSet packet, final ClientPlayNetworking.Context context) {
        if (packet.textureData().length == 16*16) {
            final GClientData data = this.getOrCreate(packet.pos(), packet.dir(), packet.depth());
            data.update(packet.textureData());
            data.updateLight(context.client().level);
        } else {
            final GClientBlock block = this.getGraffiti().get(packet.pos());
            if (block != null) {
                final List<GClientData> dataList = block.getBlockData().get(packet.dir());
                if (dataList != null) {
                    dataList.removeIf(d -> {
                        final boolean remove = d.getDepth() == packet.depth();
                        if (remove) {
                            d.close();
                        }
                        return remove;
                    });
                }
            }
        }
    }

    public void handleBlockInvalidate(final ClientBoundInvalidateBlock packet, final ClientPlayNetworking.Context context) {
        final GClientBlock block = this.getGraffiti().remove(packet.pos());
        if (block != null) {
            block.spawnParticles(context.client().level);
            block.close();
        }
    }

    public void handleChange(final BiDirectionalGraffitiChange packet, final ClientPlayNetworking.Context context) {
        final ColorComponent colorComponent = ArtUtil.generateComponentFromByte(packet.content());
        
        for (final Map.Entry<TileKey, TileChange> entry : packet.changes().entrySet()) {
            final TileKey key = entry.getKey();
            final TileChange change = entry.getValue();

            final GClientData data = this.getOrConditionalCreate(key.pos(), key.dir(), key.depth(), packet.content() == ColorComponent.CLEAR.id);
            if (data != null) {
                data.handleChange(colorComponent.argb, change);
            }
        }
    }

    public void updateLights(final ClientLevel level) {
        this.forEach(data -> {
            data.updateLight(level);
        });
    }

    public void closeAll() {
        for (final GClientBlock graffitis : this.getGraffiti().values()) {
            graffitis.close();
        }
        this.graffiti.clear();
    }

    @Override
    public void close() {
        this.closeAll();
    }
}
