package com.streetart;

/**
 * The underlying data for a single texture plane
 */
public abstract class GData {
    public final double depth;

    public GData(double depth) {
        this.depth = depth;
    }
}
