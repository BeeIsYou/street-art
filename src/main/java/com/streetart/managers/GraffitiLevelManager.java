package com.streetart.managers;

import com.streetart.StreetArt;
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

public class GraffitiLevelManager {

    private final Map<BlockPos, Map<Direction, List<GraffitiData>>> dataMap = new HashMap<>();
    private final List<TempData> dirtyData = new ArrayList<>();

    private final ServerLevel level;

    public GraffitiLevelManager(final ServerLevel level) {
        this.level = level;
    }


    /**
     * Creates (if necessary) graffiti dataHolder and populates it with the given click position.
     */
    public void createAndPopulateGraffiti(final BlockPos pos, final Vec3 clickPosition, final Direction dir, final int color) {
        final Map<Direction, List<GraffitiData>> data = this.dataMap.computeIfAbsent(pos, p -> new HashMap<>());

        final Vec3 relativePos = clickPosition.subtract(Vec3.atLowerCornerOf(pos));
        double depth = switch (dir.getAxis()) {
            case X -> relativePos.x;
            case Y -> relativePos.y;
            case Z -> relativePos.z;
        };

        if (dir.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            depth -= 1;
        }

        final List<GraffitiData> grafArr = data.computeIfAbsent(dir, d -> new ArrayList<>());
        GraffitiData gatheredData = null;
        for (final GraffitiData grafData : grafArr) {
            if (Math.abs(grafData.depth - depth) < 1E-4) {
                gatheredData = grafData;
                break;
            }
        }

        if (gatheredData == null) {
            gatheredData = new GraffitiData(depth);
        }

        gatheredData.computeChanges(pos, clickPosition, color);
        this.dirtyData.add(new TempData(gatheredData, pos, dir));
    }

    public void tick() {
        this.dirtyData.removeIf(dataHolder -> {
            if (dataHolder.data.dirty) {
                dataHolder.data.dirty = false;
                for (final ServerPlayer player : PlayerLookup.around(this.level, dataHolder.pos.getCenter(), 100)) {
                    ServerPlayNetworking.send(player, new ClientBoundGraffitUpdate(dataHolder.pos, dataHolder.dir, dataHolder.data.depth, dataHolder.data.graffitiData));
                }

            }

            return true;
        });
    }

    public record TempData(GraffitiData data, BlockPos pos, Direction dir) {

    }

    public static class GraffitiData {

        /**
         * Byte array representing a texture. Synchronized to clients when changed on the following tick.
         */
        private final byte[] graffitiData = new byte[16 * 16];

        /**
         * The depth of this graffiti relative to the associated block pos.
         */
        private final double depth;

        boolean dirty;

        public GraffitiData(final double depth) {
            this.depth = depth;
        }

        public void computeChanges(final BlockPos pos, final Vec3 clickPosition, final int color) {
            this.dirty = true;
            StreetArt.LOGGER.info("modified {}, {}, with {} color", pos, clickPosition, color);
        }
    }
}
