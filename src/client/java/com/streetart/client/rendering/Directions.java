package com.streetart.client.rendering;

import net.minecraft.core.Direction;
import org.joml.Matrix4f;

import java.util.Map;

/**
 * Micro-opt of caching the math.
 *
 * @author Ampflower
 */
final class Directions {
	static final Map<Direction, Matrix4f> matrices = Map.of(
		Direction.UP, bootstrap(Direction.UP),
		Direction.DOWN, bootstrap(Direction.DOWN),
		Direction.NORTH, bootstrap(Direction.NORTH),
		Direction.SOUTH, bootstrap(Direction.SOUTH),
		Direction.WEST, bootstrap(Direction.WEST),
		Direction.EAST, bootstrap(Direction.EAST)
	);

	private static Matrix4f bootstrap(final Direction direction) {
		return new Matrix4f().rotateAround(direction.getRotation(), 0.5f, 0.5f, 0.5f);
	}
}
