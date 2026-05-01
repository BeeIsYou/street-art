package com.streetart;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.*;

import java.util.function.ToIntFunction;

public class AllGameRules {
    public static final GameRuleCategory CATEGORY = GameRuleCategory.register(StreetArt.id("game_rules"));

    public static final GameRule<Boolean> ADVENTURE_PAINTING = registerBoolean("adventure_painting", CATEGORY, false);
    public static final GameRule<Boolean> NON_PLAYERS_ADVENTURE = registerBoolean("non_players_adventure", CATEGORY, true);
    public static final GameRule<Integer> RANDOM_DECAY_SPEED = registerInteger("random_decay_speed", CATEGORY, 0, 0, 1000);
    public static final GameRule<Boolean> ONLY_SWAG_GUYS_PAINT_ON_SECOND_LAYERS = registerBoolean("second_layer_adventure_painting", CATEGORY, false);

    private static GameRule<Boolean> registerBoolean(final String id, final GameRuleCategory category, final boolean defaultValue) {
        return register(id, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, GameRuleTypeVisitor::visitBoolean, b -> b != false ? 1 : 0);
    }

    private static GameRule<Integer> registerInteger(final String id, final GameRuleCategory category, final int defaultValue, final int min, final int max) {
        return register(id, category, GameRuleType.INT, IntegerArgumentType.integer(min, max), Codec.intRange(min, max), defaultValue, GameRuleTypeVisitor::visitInteger, i -> i);
    }

    private static <T> GameRule<T> register(final String id, final GameRuleCategory category, final GameRuleType typeHint, final ArgumentType<T> argumentType, final Codec<T> codec, final T defaultValue, final GameRules.VisitorCaller<T> visitorCaller, final ToIntFunction<T> commandResultFunction) {
        return Registry.register(BuiltInRegistries.GAME_RULE, StreetArt.id(id), new GameRule<>(
                category,
                typeHint,
                argumentType,
                visitorCaller,
                codec,
                commandResultFunction,
                defaultValue,
                FeatureFlagSet.of()
        ));
    }

    public static void init() {
    }
}
