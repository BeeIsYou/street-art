package com.streetart.schmoovement;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class WallCollideStatus {
    private Vec3 preWallCollide = Vec3.ZERO;
    private Type type = Type.NONE;
    private Type cachedType = Type.NONE;

    public void markCollision(final Vec3 movement, final Vec3 modifiedMovement) {
        this.preWallCollide = movement;
        this.type = Type.fromBeforeAndAfter(movement, modifiedMovement);
        if (this.type != Type.NONE) {
            this.cachedType = this.type.tryMerge(this.cachedType);
        }
    }

    public void testCache(final Entity entity) {
        if (!this.cachedType.canContinue(entity)) {
            this.cachedType = this.type;
        }
    }

    public void resetToNone() {
        this.type = Type.NONE;
        this.cachedType = Type.NONE;
    }

    public Type getType() {
        return this.type;
    }

    public Type getCachedType() {
        return this.cachedType;
    }

    public Vec3 getUnmodified() {
        return this.preWallCollide;
    }

    /**
     * The direction that is being moved <em>into</em>
     */
    public enum Type {
        NORTH_WEST(new Vector3d( 1, 0, -1)),
        NORTH     (new Vector3d( 0, 0, -1)),
        NORTH_EAST(new Vector3d(-1, 0, -1)),
        WEST      (new Vector3d( 1, 0,  0)),
        NONE      (new Vector3d( 0, 0,  0)),
        EAST      (new Vector3d(-1, 0,  0)),
        SOUTH_WEST(new Vector3d( 1, 0,  1)),
        SOUTH     (new Vector3d( 0, 0,  1)),
        SOUTH_EAST(new Vector3d(-1, 0,  1))
        ;

        public final Vector3dc normal;

        Type(final Vector3dc normal) {
            this.normal = normal;
        }

        public boolean isColliding() {
            return this != NONE;
        }

        public boolean isOrthogonal() {
            return this == NORTH || this == SOUTH || this == EAST || this == WEST;
        }

        public boolean isDiagonal() {
            return this == NORTH_EAST || this == NORTH_WEST || this == SOUTH_EAST || this == SOUTH_WEST;
        }

        public boolean isSimilarDirection(final Type other) {
            return this.normal.dot(other.normal) > 0;
        }

        public static Type fromBeforeAndAfter(final Vec3 beforePos, final Vec3 afterPos) {
            final double dx = beforePos.x - afterPos.x;
            final double dz = beforePos.z - afterPos.z;
            if (dx == 0) { // center
                if (dz == 0) { // center
                    return NONE;
                } else if (dz < 0) { // south
                    return SOUTH;
                } else { // north
                    return NORTH;
                }
            } else if (dx < 0) { // west
                if (dz == 0) { // center
                    return WEST;
                } else if (dz < 0) { // south
                    return SOUTH_WEST;
                } else { // north
                    return NORTH_WEST;
                }
            } else { // east
                if (dz == 0) { // center
                    return EAST;
                } else if (dz < 0) { // south
                    return SOUTH_EAST;
                } else { // north
                    return NORTH_EAST;
                }
            }
        }

        /**
         * @return NONE if can not merge, otherwise the combined direction
         */
        public Type tryMerge(final Type other) {
            if (this == other) {
                return this;
            }
            if (this.isDiagonal() && this.isSimilarDirection(other)) {
                return this;
            }
            if (other.isDiagonal() && other.isSimilarDirection(this)) {
                return other;
            }

            return switch (this) {
                case NORTH -> {
                    if (other == EAST) {
                        yield NORTH_EAST;
                    }
                    if (other == WEST) {
                        yield NORTH_WEST;
                    }
                    yield NONE;
                }
                case EAST -> {
                    if (other == NORTH) {
                        yield NORTH_EAST;
                    }
                    if (other == SOUTH) {
                        yield SOUTH_EAST;
                    }
                    yield NONE;
                }
                case SOUTH -> {
                    if (other == EAST) {
                        yield SOUTH_EAST;
                    }
                    if (other == WEST) {
                        yield SOUTH_WEST;
                    }
                    yield NONE;
                }
                case WEST -> {
                    if (other == NORTH) {
                        yield NORTH_WEST;
                    }
                    if (other == SOUTH) {
                        yield SOUTH_WEST;
                    }
                    yield NONE;
                }
                default -> NONE;
            };
        }

        public boolean canContinue(final Entity entity) {
            if (!this.isColliding()) {
                return false;
            }

            final Vector3d normal = this.normal.mul(-0.05, new Vector3d());
            final AABB testBox = entity.getBoundingBox().expandTowards(normal.x(), normal.y(), normal.z());

            return !entity.level().noBlockCollision(entity, testBox);
        }
    }
}
