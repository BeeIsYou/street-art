package com.streetart;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.streetart.commands.ClearCommand;
import com.streetart.commands.ColorComponentArgument;
import com.streetart.commands.FillCommand;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class AllCommands {
    public static void init() {
        ArgumentTypeRegistry.registerArgumentType(
                StreetArt.id("color_component"),
                ColorComponentArgument.class,
                new ColorComponentArgument.Info()
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("street_art");

        ClearCommand.register(root);
        FillCommand.register(root);

        dispatcher.register(root);
    }
}
