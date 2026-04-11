package com.streetart.managers;

import com.streetart.GManager;
import com.streetart.networking.ClientBoundGraffitUpdate;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GLevelManager extends GManager<GServerData, GServerBlock, GLevelManager> {
    private final Map<BlockPos, GServerBlock> graffiti = new HashMap<>();
    private final List<TempData> dirtyData = new ArrayList<>();

    private final ServerLevel level;

    @Override
    protected Map<BlockPos, GServerBlock> getGraffiti() {
        return this.graffiti;
    }

    public GLevelManager(final ServerLevel level) {
        this.level = level;
    }

    /**
     * Creates (if necessary) graffiti dataHolder and populates it with the given click position.
     */
    public void createAndPopulateGraffiti(final BlockPos pos, final Direction dir, final Vec3 absolutePos, final int color) {
        GServerData data = this.getOrCreate(pos, dir, absolutePos);
        data.computeChanges(pos, absolutePos, color);
        this.dirtyData.add(new TempData(data, pos, dir));
    }

    public void tick() {
        this.dirtyData.removeIf(dataHolder -> {
            if (dataHolder.data.dirty) {
                dataHolder.data.dirty = false;
                for (final ServerPlayer player : PlayerLookup.around(this.level, dataHolder.pos.getCenter(), 100)) {
                    ServerPlayNetworking.send(player, new ClientBoundGraffitUpdate(
                            dataHolder.pos,
                            dataHolder.dir,
                            dataHolder.data.depth,
                            dataHolder.data.graffitiData
                    ));
                }

            }

            return true;
        });
    }

    @Override
    public GServerBlock newBlockData(BlockPos pos) {
        return new GServerBlock(pos);
    }

    @Override
    public void close() {}

    public record TempData(GServerData data, BlockPos pos, Direction dir) { }
}
