package com.streetart.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.streetart.component.ColorComponent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.concurrent.CompletableFuture;

public class ColorComponentArgument implements ArgumentType<ColorComponent> {
    public static DynamicCommandExceptionType INVALID_COLOR = new DynamicCommandExceptionType(v -> new LiteralMessage("Invalid Color " + v));

    private ColorComponentArgument() {}

    private boolean withClear = false;
    public static ColorComponentArgument withoutClear() {
        return new ColorComponentArgument();
    }
    public static ColorComponentArgument withClear() {
        final ColorComponentArgument argument = new ColorComponentArgument();
        argument.withClear = true;
        return argument;
    }

    @Override
    public ColorComponent parse(final StringReader reader) throws CommandSyntaxException {
        final String name = reader.readUnquotedString();
        final ColorComponent color = ColorComponent.fromString(name);
        if (color == null || (!this.withClear && color == ColorComponent.CLEAR)) {
            throw INVALID_COLOR.createWithContext(reader, name);
        }
        return color;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        if (this.withClear) {
            return SharedSuggestionProvider.suggest(ColorComponent.NAMES, builder);
        } else {
            return SharedSuggestionProvider.suggest(ColorComponent.NON_CLEAR_NAMES, builder);
        }
    }

    // what the fuck
    public static class Info implements ArgumentTypeInfo<ColorComponentArgument, Info.Template> {
        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf out) {
            ByteBufCodecs.BOOL.encode(out, template.withClear);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf in) {
            return new Template(this,
                    ByteBufCodecs.BOOL.decode(in)
            );
        }

        @Override
        public void serializeToJson(Template template, JsonObject out) {
            out.addProperty("withClear", template.withClear);
        }

        @Override
        public Template unpack(ColorComponentArgument argument) {
            return new Template(this,
                    argument.withClear
            );
        }

        public static class Template implements ArgumentTypeInfo.Template<ColorComponentArgument> {
            private final Info info;
            private final boolean withClear;

            private Template(Info info, boolean withClear) {
                this.info = info;
                this.withClear = withClear;
            }

            @Override
            public ColorComponentArgument instantiate(CommandBuildContext context) {
                return this.withClear ? ColorComponentArgument.withClear() : ColorComponentArgument.withoutClear();
            }

            @Override
            public ArgumentTypeInfo<ColorComponentArgument, ?> type() {
                return this.info;
            }
        }
    }
}
