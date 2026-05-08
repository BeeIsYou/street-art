package com.streetart.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.streetart.SplashUtil;
import com.streetart.component.ColorComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class SplashCommand {
    public static void register(final LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(
            Commands.literal("splash")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
                Commands.argument("pos", Vec3Argument.vec3())
                .then(
                    Commands.argument("color", ColorComponentArgument.withClear())
                    .executes(SplashCommand::splashDefault)
                    .then(Commands.argument("range", DoubleArgumentType.doubleArg(0, 16))
                        .then(Commands.argument("rays", IntegerArgumentType.integer(1, 10000))
                            .then(Commands.argument("intensity", FloatArgumentType.floatArg(0, 10))
                                .executes(SplashCommand::splash)
                            )
                        )
                    )
                )
            )
        );
    }

    private static int splashDefault(final CommandContext<CommandSourceStack> context) {
        return splash(
                context,
                Vec3Argument.getVec3(context, "pos"),
                context.getArgument("color", ColorComponent.class),
                3, 1000, 1
        );
    }

    private static int splash(final CommandContext<CommandSourceStack> context) {
        return splash(
                context,
                Vec3Argument.getVec3(context, "pos"),
                context.getArgument("color", ColorComponent.class),
                DoubleArgumentType.getDouble(context, "range"),
                IntegerArgumentType.getInteger(context, "rays"),
                FloatArgumentType.getFloat(context, "intensity")
        );
    }

    private static int splash(final CommandContext<CommandSourceStack> context,
                              final Vec3 pos, final ColorComponent color,
                              final double range, final int rays, final float intensityScale
    ) {
        SplashUtil.createPaintSplash(
                context.getSource().getEntity(),
                context.getSource().getLevel(),
                pos,
                range, rays, intensityScale,
                SplashUtil.VariableThreshold.perlin(context.getSource().getLevel().getRandom()),
                color.id,
                _ -> true
        );

        context.getSource().sendSuccess(() -> Component.translatable("commands.street_art.splash.success"), false);

        return 1;
    }
}
