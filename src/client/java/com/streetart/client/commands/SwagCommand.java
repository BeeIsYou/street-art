package com.streetart.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.streetart.AllGraffitiLayers;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class SwagCommand {
    // dumb and hacky but im outta time :p
    public static void register(final CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("street_art_client")
            .then(ClientCommands.literal("swag_mode")
                .executes(context -> {
                    if (AllGraffitiLayers.SWAG_MODE) {
                        AllGraffitiLayers.SWAG_MODE = false;
                        context.getSource().sendFeedback(Component.translatable("commands.street_art_client.swag_mode.disabled"));
                    }  else {
                        AllGraffitiLayers.SWAG_MODE = true;
                        context.getSource().sendFeedback(Component.translatable("commands.street_art_client.swag_mode.enabled"));
                    }
                    return 1;
                })
                .then(ClientCommands.argument("set", BoolArgumentType.bool())
                    .executes(context -> {
                        final boolean set = BoolArgumentType.getBool(context, "set");
                        if (set == AllGraffitiLayers.SWAG_MODE) {
                            context.getSource().sendError(Component.translatable("commands.street_art_client.swag_mode.already_in_mode"));
                            return 0;
                        }
                        AllGraffitiLayers.SWAG_MODE = set;
                        if (set) {
                            context.getSource().sendFeedback(Component.translatable("commands.street_art_client.swag_mode.enabled"));
                        } else {
                            context.getSource().sendFeedback(Component.translatable("commands.street_art_client.swag_mode.disabled"));
                        }
                        return 1;
                    })

                )
            )
        );
    }
}
