package com.streetart.client;

import com.streetart.AllItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class StreetArtDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(StreetArtLangProvider::new);
		pack.addProvider(StreetArtModelProvider::new);
	}

	private static class StreetArtLangProvider extends FabricLanguageProvider {
		protected StreetArtLangProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(packOutput, registryLookup);
		}

		@Override
		public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder translationBuilder) {
			translationBuilder.add(AllItems.SPRAY_CAN, "Spray Can");

			translationBuilder.add("lore.street_art.spray_can",
					"The perfect tool for freeform expression! Sneak + Use to switch modes"
			);

			translationBuilder.add(AllItems.CREATIVE_TAB_KEY, "Street Art");

			translationBuilder.add("street_art.spray_can.unfocused", "Unfocused");
			translationBuilder.add("street_art.spray_can.focused", "Focused");

			translationBuilder.add("street_art.toast.atlas_full.title", "Atlas Full");
			translationBuilder.add("street_art.toast.atlas_full.body", "Spray Paint textures forced to clear");
		}
	}

	private static class StreetArtModelProvider extends FabricModelProvider {
		public StreetArtModelProvider(FabricPackOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {

		}

		@Override
		public void generateItemModels(ItemModelGenerators itemModelGenerators) {
		}
	}
}
