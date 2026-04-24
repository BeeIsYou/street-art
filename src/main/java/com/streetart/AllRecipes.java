package com.streetart;

import com.streetart.recipe.TrackDuplicateRecipe;
import com.streetart.recipe.TrackDyeRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class AllRecipes {
    public static void init() {
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                StreetArt.id("crafting_special_tape_duplicate"),
                TrackDuplicateRecipe.SERIALIZER
        );
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                StreetArt.id("crafting_special_tape_dye"),
                TrackDyeRecipe.SERIALIZER
        );
    }
}
