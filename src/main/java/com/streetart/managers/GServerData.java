package com.streetart.managers;

import com.streetart.GData;
import com.streetart.StreetArt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.nio.ByteBuffer;

public class GServerData extends GData {
    /**
     * Byte array representing a texture. Synchronized to clients when changed on the following tick.
     */
    public final byte[] graffitiData = new byte[4 * 16 * 16];

    boolean dirty;

    public GServerData(double depth) {
        super(depth);
    }

    public void computeChanges(final BlockPos pos, final Vec3 clickPosition, final int color) {
        this.dirty = true;
        ByteBuffer buffer = ByteBuffer.wrap(this.graffitiData);
        for (int i = 0; i < 16 * 16; i++) {
            buffer.putInt(color);
        }
        StreetArt.LOGGER.info("modified {}, {}, with {} color", pos, clickPosition, color);
    }
}
