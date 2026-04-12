package com.streetart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class ArtUtil {
    public static Vector2i calculatePixelCoordinates(BlockHitResult hitResult) {
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
        return new Vector2i(x, y);
    }

    public static double calculateDepth(BlockHitResult hitResult) {
        final Vec3 relativePos = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(hitResult.getBlockPos()));
        double depth = switch (hitResult.getDirection().getAxis()) {
            case X -> relativePos.x;
            case Y -> relativePos.y;
            case Z -> relativePos.z;
        };

        if (hitResult.getDirection().getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            depth = 1 - depth;
        }
        return depth;
    }
}
