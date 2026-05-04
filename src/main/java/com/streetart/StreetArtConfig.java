package com.streetart;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;

public class StreetArtConfig {
    public static ConfigClassHandler<StreetArtConfig> HANDLER = ConfigClassHandler.<StreetArtConfig>createBuilder(StreetArtConfig.class)
            .id(StreetArt.id("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("street_art.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Last resort for if something goes terribly wrong. Please report the issue and don't just ignore it!")
    public boolean ignoreEverything = false;

    public static boolean ignoreEverything() {
        return HANDLER.instance().ignoreEverything;
    }
}
