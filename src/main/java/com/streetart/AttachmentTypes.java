package com.streetart;

import com.streetart.managers.GServerChunkManager;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import static com.streetart.StreetArt.id;

public class AttachmentTypes {

    public static final AttachmentType<GServerChunkManager> CHUNK_MANAGER = AttachmentRegistry.create(id("graffiti_data"), (AttachmentRegistry.Builder<GServerChunkManager> b) ->
            b.initializer(GServerChunkManager::new)
                    .persistent(GServerChunkManager.CODEC)
                    .buildAndRegister(id("graffiti_data")));


    public static void init() {
    }
}
