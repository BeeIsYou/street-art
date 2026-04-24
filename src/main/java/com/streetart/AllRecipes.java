package com.streetart;

import com.streetart.recipe.TapeDuplicateRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class AllRecipes {
    public static void init() {
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                StreetArt.id("crafting_special_tape_duplicate"),
                TapeDuplicateRecipe.SERIALIZER
        );
    }
}
