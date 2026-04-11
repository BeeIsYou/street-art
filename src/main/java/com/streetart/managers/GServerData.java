package com.streetart.managers;

import com.streetart.GData;
import com.streetart.StreetArt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

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

    public void computeChanges(final BlockPos pos, final Vec3 clickPosition, Direction clickDir, final int color) {
        this.dirty = true;
        Vector3f relative = new Vector3f(
                (float)(clickPosition.x - pos.getX()),
                (float)(clickPosition.y - pos.getY()),
                (float)(clickPosition.z - pos.getZ())
        );
        Vec2 plane = switch (clickDir) {
            case DOWN -> new Vec2(relative.x, 1-relative.z);
            case UP -> new Vec2(relative.x, relative.z);
            case NORTH -> new Vec2(1-relative.x, 1-relative.y);
            case SOUTH -> new Vec2(relative.x, 1-relative.y);
            case WEST -> new Vec2(relative.z, 1-relative.y);
            case EAST -> new Vec2(1-relative.z, 1-relative.y);
        };
        int x = (int)(plane.x * 16);
        int y = (int)(plane.y * 16);
        int i = (x + y * 16) * 4;
        if (0 <= i && i < 16*16*4) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(color);
            this.graffitiData[i  ] = buffer.get(0);
            this.graffitiData[i+1] = buffer.get(1);
            this.graffitiData[i+2] = buffer.get(2);
            this.graffitiData[i+3] = buffer.get(3);
        }
        StreetArt.LOGGER.info("modified {}, {}, with {} color", pos, clickPosition, color);
    }
}
