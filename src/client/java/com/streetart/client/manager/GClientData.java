package com.streetart.client.manager;

import com.google.common.primitives.Ints;
import com.streetart.GData;
import com.streetart.client.StreetArtClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class GClientData extends GData implements AutoCloseable {
    public final Direction dir;
    public final BlockPos pos;
    public final int id;

    public int light = -1;

    public GClientData(Direction dir, double depth, BlockPos pos, int id) {
        super(depth);
        this.dir = dir;
        this.pos = pos;
        this.id = id;
    }

    public void update(byte[] data) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int i = (x + y*16) * 4;
                int argb = Ints.fromBytes(data[i], data[i+1], data[i+2], data[i+3]);
                this.setPixel(x, y, argb);
            }
        }
    }

    public void updateLight(ClientLevel level) {
        this.light = LevelRenderer.getLightCoords(level,
                this.getDepth() == 1 ? this.pos.relative(this.dir) : this.pos
        );
    }

    public void computeChanges(BlockHitResult hitResult, final int color) {
        final BlockPos pos = hitResult.getBlockPos();
        final Vec3 clickPosition = hitResult.getLocation();
        Vector3f relative = new Vector3f(
                (float)(clickPosition.x - pos.getX()),
                (float)(clickPosition.y - pos.getY()),
                (float)(clickPosition.z - pos.getZ())
        );
        Vec2 plane = switch (hitResult.getDirection()) {
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
            this.setPixel(x, y, color);
            StreetArtClient.textureManager.markModified(this);
        }
    }

    public void setPixel(int x, int y, int color) {
        StreetArtClient.textureManager.tileAtlasManager.setPixel(this.id, x, y, color);
    }

    public int getPixel(int x, int y) {
        return StreetArtClient.textureManager.tileAtlasManager.getPixel(this.id, x, y);
    }

    public byte[] getTextureData() {
        return StreetArtClient.textureManager.tileAtlasManager.getPixelData(this.id);
    }

    @Override
    public void close() {
        StreetArtClient.textureManager.tileAtlasManager.freeID(this.id);
    }
}
