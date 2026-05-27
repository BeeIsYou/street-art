package com.streetart.client.manager;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;

import java.util.function.Function;

/**
 * @author Ampflower
 **/
public final class GClientSection implements AutoCloseable {
	// Admittedly this being a map is somewhat suboptimal compared to a flat array of 4096 entries,
	// but this allows itself to shrink when it's sparse.
	private final Short2ObjectOpenHashMap<GClientBlock> blocks = new Short2ObjectOpenHashMap<>();

	public GClientBlock get(final BlockPos pos) {
		return this.blocks.get(toIndex(pos));
	}

	public void put(final BlockPos pos, final GClientBlock block) {
		this.blocks.put(toIndex(pos), block);
	}

	public GClientBlock computeIfAbsent(final BlockPos pos, final Function<BlockPos, GClientBlock> creator) {
		return this.blocks.computeIfAbsent(toIndex(pos), _ -> creator.apply(pos));
	}

	public GClientBlock remove(final BlockPos pos) {
		return this.blocks.remove(toIndex(pos));
	}

	public ShortSet getRawPositions() {
		return this.blocks.keySet();
	}

	public ObjectCollection<GClientBlock> getBlocks() {
		return this.blocks.values();
	}

	public int size() {
		return this.blocks.size();
	}

	public boolean isEmpty() {
		return this.blocks.isEmpty();
	}

	private static short toIndex(final BlockPos pos) {
		return (short)(((pos.getY() & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15));
	}

	static BlockPos.MutableBlockPos fromIndex(BlockPos.MutableBlockPos pos, int x, int y, int z, short index) {
		return pos.set(
			x | (index & 15),
			y | ((index >> 8) & 15),
			z | ((index >> 4) & 15)
		);
	}

	@Override
	public void close() {
		for (final GClientBlock graffitis : this.getBlocks()) {
			graffitis.close();
		}

		this.blocks.clear();
	}
}
