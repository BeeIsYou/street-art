package com.streetart.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.streetart.AllGraffitiLayers;
import com.streetart.ArtUtil;
import com.streetart.component.ColorComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class FillCommand {
    //TODO: add layer attribute!
    public static void register(final LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(
                Commands.literal("fill")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.argument("from", BlockPosArgument.blockPos())
                                        .then(
                                                Commands.argument("to", BlockPosArgument.blockPos())
                                                        .then(
                                                                Commands.argument("color", ColorComponentArgument.withoutClear())
                                                                        .executes(FillCommand::fillRegion)
                                                        )
                                        )
                        )
        );
    }

    private static int fillRegion(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final ServerLevel level = context.getSource().getLevel();
        final BlockPos a = BlockPosArgument.getLoadedBlockPos(context, "from");
        final BlockPos b = BlockPosArgument.getLoadedBlockPos(context, "to");
        final ColorComponent color = context.getArgument("color", ColorComponent.class);
        int count = 0;
        for (final BlockPos blockPos : BlockPos.betweenClosed(a, b)) {
            final List<ArtUtil.ShapeFaces> faces = ArtUtil.gatherShapeFaces(level.getBlockState(blockPos).getCollisionShape(level, blockPos));
            // todo layer
            if (ArtUtil.latherInPaint(AllGraffitiLayers.DEFAULT_LAYER.identifier(), context.getSource().getEntity(),
                    level, faces, blockPos.immutable(), color.id)
            ) {
                count++;
            }
        }

        final int finalCount = count;
        context.getSource().sendSuccess(() -> Component.translatable("commands.street_art.fill.success", finalCount), false);
        return count;
    }
}
