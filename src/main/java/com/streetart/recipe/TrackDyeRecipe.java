package com.streetart.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.AllDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TrackDyeRecipe extends CustomRecipe {
    public static final MapCodec<TrackDyeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("target").forGetter(r -> r.target),
            Ingredient.CODEC.fieldOf("dye").forGetter(r -> r.dye)
    ).apply(instance, TrackDyeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TrackDyeRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            r -> r.target,
            Ingredient.CONTENTS_STREAM_CODEC,
            r -> r.dye,
            TrackDyeRecipe::new
    );

    public static final RecipeSerializer<TrackDyeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient target;
    private final Ingredient dye;

    public TrackDyeRecipe(final Ingredient target, final Ingredient dye) {
        this.target = target;
        this.dye = dye;
    }

    @Override
    public boolean matches(final CraftingInput input, final Level level) {
        int targets = 0;
        int dyes = 0;
        for (final ItemStack item : input.items()) {
            if (this.target.test(item) && item.has(AllDataComponents.TRACK_RECORDING)) {
                targets++;
                if (targets > 1) {
                    return false;
                }
            } else if (this.dye.test(item) && item.has(DataComponents.DYE)) {
                dyes++;
            } else if (!item.isEmpty()) {
                return false;
            }
        }
        return targets > 0 && (dyes == 1 || dyes == 2);
    }

    @Override
    public ItemStack assemble(final CraftingInput input) {
        ItemStack target = ItemStack.EMPTY;
        DyeColor dye1 = null;
        DyeColor dye2 = null;
        for (final ItemStack item : input.items()) {
            if (this.target.test(item) && item.has(AllDataComponents.TRACK_RECORDING)) {
                if (!target.isEmpty()) {
                    return ItemStack.EMPTY;
                } else {
                    target = item;
                }
            } else if (this.dye.test(item) && item.has(DataComponents.DYE)) {
                if (dye1 == null) {
                    dye1 = item.get(DataComponents.DYE);
                } else if (dye2 == null) {
                    dye2 = item.get(DataComponents.DYE);
                } else {
                    return ItemStack.EMPTY;
                }
            } else if (!item.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        if (target.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (dye1 == null) {
            return ItemStack.EMPTY;
        }

        if (dye2 == null) {
            dye2 = dye1;
        }

        final ItemStack result = target.copy();
        result.set(AllDataComponents.TRACK_RECORDING, result.get(AllDataComponents.TRACK_RECORDING).redye(dye1, dye2));
        return result;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
