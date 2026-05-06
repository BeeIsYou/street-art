package com.streetart;

import com.streetart.managers.GServerChunkManager;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

import static com.streetart.StreetArt.id;
import static com.streetart.StreetArt.recordingManager;

public class AttachmentTypes {

    public static final AttachmentType<GServerChunkManager> CHUNK_MANAGER = register(id("graffiti_data"), builder -> {
        builder.initializer(GServerChunkManager::new)
                .persistent(GServerChunkManager.CODEC);
    });

    private static <T> AttachmentType<T> register(Identifier id, Consumer<AttachmentRegistry.Builder<T>> consumer) {
        return AttachmentRegistry.create(id, consumer);
    }

    public static void init() {
    }
}
