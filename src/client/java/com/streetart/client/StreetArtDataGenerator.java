package com.streetart.client;

import com.streetart.AllItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class StreetArtDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(final FabricDataGenerator fabricDataGenerator) {
		final FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(StreetArtLangProvider::new);
		pack.addProvider(StreetArtModelProvider::new);
	}

	private static class StreetArtLangProvider extends FabricLanguageProvider {
		protected StreetArtLangProvider(final FabricPackOutput packOutput, final CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(packOutput, registryLookup);
		}

		@Override
		public void generateTranslations(final HolderLookup.Provider provider, final TranslationBuilder translationBuilder) {
			translationBuilder.add(AllItems.SPRAY_CAN, "Spray Can");
			translationBuilder.add(AllItems.WATER_BALLOON, "Water Balloon");
			translationBuilder.add(AllItems.PAINT_BALLOON, "Paint Balloon");
			translationBuilder.add(AllItems.CREATIVE_PRESSURE_WASHER, "Creative Pressure Washer");

			translationBuilder.add("lore.street_art.spray_can",
                    """
                            The perfect tool for freeform expression!
                            Hold right click to spray in a large cone
                            Hold left click to spray in a small cone"""
			);
			translationBuilder.add("lore.street_art.pressure_washer",
					"""
                            The perfect tool for freeform expression!
                            Hold right click to clean in a horizontal line
                            Hold left click to clean in a vertical line"""
			);
			translationBuilder.add("lore.street_art.water_balloom",
					"Throw to wash away a small area of paint"
			);
			translationBuilder.add("lore.street_art.paint_balloom",
					"Throw to spread a small area of paint"
			);
			translationBuilder.add("lore.street_art.creative_pressure_washer",
                    """
                            The perfect tool for crushing freeform expression! Operator only
                            R-Click the air to reduce radius
                            Sneak + R-Click in the air to increase radius"""
			);

			translationBuilder.add(AllItems.CREATIVE_TAB_KEY, "Street Art");

			translationBuilder.add("street_art.toast.atlas_full.title", "Atlas Full");
			translationBuilder.add("street_art.toast.atlas_full.body", "Spray Paint textures forced to clear");
		}
	}

	private static class StreetArtModelProvider extends FabricModelProvider {
		public StreetArtModelProvider(final FabricPackOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(final BlockModelGenerators blockModelGenerators) {

		}

		@Override
		public void generateItemModels(final ItemModelGenerators itemModelGenerators) {
			itemModelGenerators.generateFlatItem(AllItems.WATER_BALLOON, ModelTemplates.FLAT_ITEM);
		}
	}
}
