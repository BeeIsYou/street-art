package com.streetart.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.streetart.AllDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TrackDuplicateRecipe extends CustomRecipe {
    public static final MapCodec<TrackDuplicateRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("copy_from").forGetter(r -> r.copyFrom),
            Ingredient.CODEC.fieldOf("copy_onto").forGetter(r -> r.copyOnto)
    ).apply(instance, TrackDuplicateRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TrackDuplicateRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            r -> r.copyFrom,
            Ingredient.CONTENTS_STREAM_CODEC,
            r -> r.copyOnto,
            TrackDuplicateRecipe::new
    );

    public static final RecipeSerializer<TrackDuplicateRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient copyFrom;
    private final Ingredient copyOnto;

    public TrackDuplicateRecipe(final Ingredient copyFrom, final Ingredient copyOnto) {
        this.copyFrom = copyFrom;
        this.copyOnto = copyOnto;
    }

    @Override
    public boolean matches(final CraftingInput input, final Level level) {
        boolean hasSource = false;
        int conversionCount = 1;
        for (final ItemStack item : input.items()) {
            if (this.copyFrom.test(item)) {
                if (item.has(AllDataComponents.TRACK_RECORDING)) {
                    if (hasSource) {
                        return false;
                    }
                    hasSource = true;
                } else {
                    return false;
                }
            } else if (this.copyOnto.test(item)) {
                conversionCount++;
            } else if (!item.isEmpty()) {
                return false;
            }
        }
        return hasSource && conversionCount > 1;
    }

    @Override
    public ItemStack assemble(final CraftingInput input) {
        ItemStack source = ItemStack.EMPTY;
        int conversionCount = 1;
        for (final ItemStack item : input.items()) {
            if (this.copyFrom.test(item)) {
                if (item.has(AllDataComponents.TRACK_RECORDING)) {
                    if (!source.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    source = item;
                } else {
                    return ItemStack.EMPTY;
                }
            } else if (this.copyOnto.test(item)) {
                conversionCount++;
            } else if (!item.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        if (source.isEmpty() || conversionCount <= 1) {
            return ItemStack.EMPTY;
        }
        return source.copyWithCount(conversionCount);
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
