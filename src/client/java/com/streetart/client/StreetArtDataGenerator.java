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
			translationBuilder.add(AllItems.PRESSURE_WASHER, "Pressure Washer");
			translationBuilder.add(AllItems.WATER_BALLOON, "Water Balloon");
			translationBuilder.add(AllItems.PAINT_BALLOON, "Paint Balloon");
			translationBuilder.add(AllItems.CREATIVE_PRESSURE_WASHER, "Creative Pressure Washer");
			translationBuilder.add(AllItems.SEALANT, "Sealant");
			translationBuilder.add(AllItems.PERMIT_WAND, "Permit Wand");
			translationBuilder.add(AllItems.DENY_WAND, "Deny Wand");

			translationBuilder.add("lore.street_art.spray_can",
                    """
                            The perfect tool for freeform expression
                            Hold right click to spray in a large cone
                            Hold left click to spray in a small cone"""
			);
			translationBuilder.add("lore.street_art.pressure_washer",
					"""
                            The perfect tool for cleaning up freeform expression
                            Hold right click to clean in a horizontal line
                            Hold left click to clean in a vertical line"""
			);
			translationBuilder.add("lore.street_art.water_balloon",
					"Throw to wash away a small area of paint"
			);
			translationBuilder.add("lore.street_art.paint_balloon",
					"Throw to spread a small area of paint"
			);
			translationBuilder.add("lore.street_art.creative_pressure_washer",
                    """
                            The superior tool for crushing freeform expression (Operator only)
                            R-Click a block to remove all paint from all faces in a radius
                            R-Click the air to reduce the radius
                            Sneak + R-Click in the air to increase the radius"""
			);
			translationBuilder.add("lore.street_art.sealant",
					"""
							Preserves your art for generations to come (Operator only)
							Hold R-Click and drag to define a region
							L-Click on a region to remove
							Spray paint within will not degrade over time"""
			);
			translationBuilder.add("lore.street_art.permit_wand",
					"""
							Sorry I got lazy (Operator only)
							Hold R-Click and drag to define a region
							L-Click on a region to remove
							Adventure mode players can modify paint on blocks within"""
			);
			translationBuilder.add("lore.street_art.deny_wand",
					"""
							Sorry I got lazy (Operator only)
							Hold R-Click and drag to define a region
							L-Click on a region to remove
							Only opped players can modify paint on blocks within"""
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
			itemModelGenerators.generateFlatItem(AllItems.SEALANT, ModelTemplates.FLAT_ITEM);
			itemModelGenerators.generateFlatItem(AllItems.PERMIT_WAND, ModelTemplates.FLAT_ITEM);
			itemModelGenerators.generateFlatItem(AllItems.DENY_WAND, ModelTemplates.FLAT_ITEM);
		}
	}
}
