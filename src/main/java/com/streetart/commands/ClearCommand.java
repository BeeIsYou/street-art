package com.streetart.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.streetart.AttachmentTypes;
import com.streetart.managers.GServerChunkManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class ClearCommand {
    public static void register(final LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(
            Commands.literal("clear")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
                Commands.argument("from", BlockPosArgument.blockPos())
                .then(
                    Commands.argument("to", BlockPosArgument.blockPos())
                    .executes(ClearCommand::clearRegion)
                )
            )
        );
    }

    private static int clearRegion(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final ServerLevel level = context.getSource().getLevel();
        final BlockPos a = BlockPosArgument.getLoadedBlockPos(context, "from");
        final BlockPos b = BlockPosArgument.getLoadedBlockPos(context, "to");
        int count = 0;
        for (final BlockPos blockPos : BlockPos.betweenClosed(a, b)) {
            final GServerChunkManager manager = level.getChunk(blockPos).getAttached(AttachmentTypes.CHUNK_MANAGER);
            if (manager != null) {
                if (manager.markForRemoval(blockPos.immutable())) {
                    count++;
                }
            }
        }

        final int finalCount = count;
        context.getSource().sendSuccess(() -> Component.literal("Explodiated graffiti from " + finalCount + " blocks"), false);
        return count;
    }
}
