package com.streetart;

/**
 * The underlying data for a single texture plane
 */
public abstract class GData {
    private final double depth;

    public GData(double depth) {
        this.depth = depth;
    }

    public double getDepth() {
        return depth;
    }
}
