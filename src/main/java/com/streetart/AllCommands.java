package com.streetart;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.streetart.commands.*;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class AllCommands {
    public static void init() {
        ArgumentTypeRegistry.registerArgumentType(
                StreetArt.id("color_component"),
                ColorComponentArgument.class,
                new ColorComponentArgument.Info()
        );
        ArgumentTypeRegistry.registerArgumentType(
                StreetArt.id("count_type"),
                CountCommand.CountTypeArgument.class,
                SingletonArgumentInfo.contextFree(CountCommand.CountTypeArgument::new)
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("street_art");

        ClearCommand.register(root);
        FillCommand.register(root);
        CountCommand.register(root);
        CloneCommand.register(root);
        SplashCommand.register(root);

        dispatcher.register(root);
    }
}
