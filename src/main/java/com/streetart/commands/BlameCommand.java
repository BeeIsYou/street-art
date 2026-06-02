package com.streetart.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.streetart.AttachmentTypes;
import com.streetart.managers.GServerChunkManager;
import com.streetart.managers.data.GServerBlock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public class BlameCommand {
    public static void register(final LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(
            Commands.literal("blame")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
                Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(BlameCommand::blame)
            )
        );
    }

    private static int blame(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final ServerLevel level = source.getLevel();
        final BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        final GServerChunkManager manager = level.getChunk(pos).getAttached(AttachmentTypes.CHUNK_MANAGER);

        if (manager == null) {
            source.sendFailure(Component.translatable("commands.street_art.blame.fail.no_uuid"));
            return 0;
        }

        final GServerBlock block = manager.getBlock(pos);

        if (block == null) {
            source.sendFailure(Component.translatable("commands.street_art.blame.fail.no_uuid"));
            return 0;
        }

        final UUID uuid = block.getLastModifiedUUID();

        if (uuid == null) {
            source.sendFailure(Component.translatable("commands.street_art.blame.fail.no_uuid"));
            return 0;
        }

        final Player player = level.getServer().getPlayerList().getPlayer(uuid);
        if (player == null) {
            source.sendSystemMessage(Component.translatable("commands.street_art.blame.profile_fetch", ComponentUtils.copyOnClickText(uuid.toString())));
            final MinecraftServer server = level.getServer();
            final ProfileResolver resolver = server.services().profileResolver();
            Util.nonCriticalIoPool().execute(
                () -> {
                    final Optional<GameProfile> result = resolver.fetchById(uuid);
                    server.execute(
                        () -> result.ifPresentOrElse(
                            profile -> source.sendSuccess(() -> Component.translatable("commands.street_art.blame.success.player", ComponentUtils.copyOnClickText(profile.name())), false),
                            () -> source.sendFailure(Component.translatable("commands.street_art.blame.fail.player", ComponentUtils.copyOnClickText(uuid.toString())))
                        )
                    );
                }
            );
        } else {
            source.sendSuccess(() -> Component.translatable("commands.street_art.blame.success.player", ComponentUtils.copyOnClickText(player.getPlainTextName())), false);
        }

        return 1;
    }
}
