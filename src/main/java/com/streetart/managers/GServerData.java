package com.streetart.managers;

import com.streetart.GData;

public class GServerData extends GData {
    /**
     * Byte array representing a texture. Synchronized to clients when changed on the following tick.
     */
    public final byte[] graffitiData = new byte[4 * 16 * 16];

    boolean dirty;

    public GServerData(double depth) {
        super(depth);
    }
}
