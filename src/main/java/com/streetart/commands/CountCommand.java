package com.streetart.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.streetart.AttachmentTypes;
import com.streetart.component.ColorComponent;
import com.streetart.managers.GServerChunkManager;
import com.streetart.managers.data.GServerBlock;
import com.streetart.managers.data.GServerDataHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CountCommand {
    public static void register(final LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(
            Commands.literal("count")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
                Commands.argument("from", BlockPosArgument.blockPos())
                .then(
                    Commands.argument("to", BlockPosArgument.blockPos())
                    .then(
                        Commands.argument("type", new CountTypeArgument())
                        .executes(CountCommand::countRegionAny)
                        .then(
                            Commands.argument("color", ColorComponentArgument.withoutClear())
                            .executes(CountCommand::countRegionColor)
                        )
                    )
                )
            )
        );
    }

    private static int countRegionAny(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final ServerLevel level = context.getSource().getLevel();
        final BlockPos a = BlockPosArgument.getLoadedBlockPos(context, "from");
        final BlockPos b = BlockPosArgument.getLoadedBlockPos(context, "to");
        final CountType type = context.getArgument("type", CountType.class);
        int c = countRegion(level, a, b, type, null);
        context.getSource().sendSuccess(() -> Component.translatable("commands.street_art.count.any_success", c, type.name), false);
        return c;
    }

    private static int countRegionColor(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final ServerLevel level = context.getSource().getLevel();
        final BlockPos a = BlockPosArgument.getLoadedBlockPos(context, "from");
        final BlockPos b = BlockPosArgument.getLoadedBlockPos(context, "to");
        final CountType type = context.getArgument("type", CountType.class);
        final ColorComponent color = context.getArgument("color", ColorComponent.class);
        int c = countRegion(level, a, b, type, color);
        context.getSource().sendSuccess(() -> Component.translatable("commands.street_art.count.color_success", c, type.name, color.name), false);
        return c;
    }

    private static int countRegion(final ServerLevel level, final BlockPos from, final BlockPos to, final CountType type,
                                   final @Nullable ColorComponent color) throws CommandSyntaxException {
        int count = 0;
        for (final BlockPos blockPos : BlockPos.betweenClosed(from, to)) {
            final GServerChunkManager manager = level.getChunk(blockPos).getAttached(AttachmentTypes.CHUNK_MANAGER);
            if (manager != null) {
                final GServerBlock block = manager.getBlock(blockPos);
                if (block != null) {
                    count += countBlock(block, type, color);
                }
            }
        }
        return count;
    }

    private static int countBlock(final GServerBlock block, final CountType type, final @Nullable ColorComponent color) {
        int c = 0;

        for (Map.Entry<Direction, List<GServerDataHolder>> entries : block.getImmutableIterator()) {
            for (final GServerDataHolder face : entries.getValue()) {
                final int dc = countFace(face, type, color);

                if (dc > 0) {
                    if (type == CountType.BLOCKS) {
                        return 1;
                    } else if (type == CountType.FACES) {
                        c++;
                        continue;
                    }
                }

                c += dc;
            }
        }

        return c;
    }

    private static int countFace(final GServerDataHolder face, final CountType type, final @Nullable ColorComponent color) {
        int c = 0;
        final ByteBuffer buf = face.getGraffitiData();
        buf.position(0);
        for (int i = 0; i < 16*16; i++) {
            final byte b = buf.get();
            if (b == ColorComponent.CLEAR.id) {
                continue;
            }
            if (color == null || (color.id == b)) {
                if (type != CountType.PIXELS) {
                    return 1;
                }
                c++;
            }
        }
        return c;
    }

    public enum CountType implements StringRepresentable {
        BLOCKS("blocks"),
        FACES("faces"),
        PIXELS("pixels");

        public static final Codec<CountType> CODEC = StringRepresentable.fromEnum(CountType::values);

        public final String name;
        CountType(final String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static class CountTypeArgument extends StringRepresentableArgument<CountType> {
        public CountTypeArgument() {
            super(CountType.CODEC, CountType::values);
        }
    }
}
