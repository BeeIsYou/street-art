package com.streetart.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.streetart.AttachmentTypes;
import com.streetart.managers.GServerChunkManager;
import com.streetart.managers.data.GServerBlock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CloneCommand {
    private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(Component.translatable("commands.street_art.clone.overlap"));

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(
                Commands.literal("clone")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.argument("begin", BlockPosArgument.blockPos())
                                        .then(
                                                Commands.argument("end", BlockPosArgument.blockPos())
                                                        .then(
                                                                Commands.argument("destination", BlockPosArgument.blockPos())
                                                                        .executes(CloneCommand::cloneRegion)
                                                        )
                                        )
                        )
        );
    }

    public static int cloneRegion(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final ServerLevel level = context.getSource().getLevel();

        final BlockPos begin = BlockPosArgument.getLoadedBlockPos(context, "begin");
        final BlockPos end = BlockPosArgument.getLoadedBlockPos(context, "end");
        final BoundingBox from = BoundingBox.fromCorners(begin, end);
        final BlockPos destBegin = BlockPosArgument.getLoadedBlockPos(context, "destination");
        final BlockPos destEnd = destBegin.offset(from.getLength());
        final BoundingBox to = BoundingBox.fromCorners(destBegin, destEnd);
        if (from.intersects(to)) {
            throw ERROR_OVERLAP.create();
        }

        final BlockPos offset = new BlockPos(to.minX() - from.minX(), to.minY() - from.minY(), to.minZ() - from.minZ());
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int copyCount = 0;

        for (int z = from.minZ(); z <= from.maxZ(); z++) {
            for (int y = from.minY(); y <= from.maxY(); y++) {
                for (int x = from.minX(); x <= from.maxX(); x++) {
                    pos.set(x, y, z);

                    final GServerChunkManager fromManager = level.getChunk(pos).getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER);
                    final GServerBlock fromBlock = fromManager.getBlock(pos);
                    if (fromBlock != null) {
                        pos.move(offset);
                        final GServerChunkManager toManager = level.getChunk(pos).getAttachedOrCreate(AttachmentTypes.CHUNK_MANAGER);
                        toManager.copyTo(pos.immutable(), fromBlock);
                        copyCount++;
                    }
                }
            }
        }

        final int finalCopyCount = copyCount;
        context.getSource().sendSuccess(() -> Component.translatable("commands.street_art.clone.success", finalCopyCount), false);
        return copyCount;
    }
}
