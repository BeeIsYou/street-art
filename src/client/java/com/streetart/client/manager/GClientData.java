package com.streetart.client.manager;

import com.google.common.primitives.Ints;
import com.streetart.GData;
import com.streetart.StreetArt;
import com.streetart.client.StreetArtClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

public class GClientData extends GData implements AutoCloseable {
    public final Direction dir;
    public final Vec3 pos;
    public final int id;
    public final Identifier location;
    private final DynamicTexture texture;

    public int light = 0;

    public GClientData(Direction dir, double depth, Vec3 pos, int id, TextureManager textureManager) {
        super(depth);
        this.dir = dir;
        this.pos = pos;
        this.id = id;
        this.location = StreetArt.id("graffiti/" + this.id);
        this.texture = new DynamicTexture(() -> "Graffiti " + this.id, 16, 16, true);
        textureManager.register(this.location, this.texture);
    }

    public void update(byte[] data) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int i = (x + y*16) * 4;
                int argb = Ints.fromBytes(data[i], data[i+1], data[i+2], data[i+3]);
                this.texture.getPixels().setPixel(x, y, argb);
            }
        }
        this.texture.upload();
    }

    public void updateLight(ClientLevel level) {
        this.light = LevelRenderer.getLightCoords(level, BlockPos.containing(this.pos));
    }

    public void upload() {
        this.texture.upload();
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
            this.texture.getPixels().setPixel(x, y, color);
            StreetArtClient.textureManager.markModified(this);
        }
    }

    public byte[] getTextureData() {
        byte[] data = new byte[16*16*4];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        for (int i : this.texture.getPixels().getPixelsABGR()) {
            buffer.putInt(i);
        }
        return data;
    }

    @Override
    public void close() {
        this.texture.close();
    }
}
