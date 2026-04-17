package com.streetart.client;

import com.streetart.AllItems;
import com.streetart.AllTags;
import com.streetart.StreetArt;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StreetArtDataGenerator implements DataGeneratorEntrypoint {
	public static final List<Item> dyesInOrder = List.of(
			Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE,
			Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE,
			Items.LIGHT_GRAY_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE,
			Items.BROWN_DYE, Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE
	);

	@Override
	public void onInitializeDataGenerator(final FabricDataGenerator fabricDataGenerator) {
		final FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(StreetArtLangProvider::new);
		pack.addProvider(StreetArtModelProvider::new);
		pack.addProvider(StreetArtTagProvider::new);
		pack.addProvider(StreetArtRecipeProvider::new);
	}

	private static class StreetArtLangProvider extends FabricLanguageProvider {
		protected StreetArtLangProvider(final FabricPackOutput packOutput, final CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(packOutput, registryLookup);
		}

		@Override
		public void generateTranslations(final HolderLookup.Provider provider, final TranslationBuilder translationBuilder) {
			AllItems.SPRAY_CANS.forEach((color, item) -> {
				translationBuilder.add(item, StringUtils.capitalize(color.getName() + " Spray Can"));
				translationBuilder.add("lore.street_art." + color.getName() + "_spray_can",
						"""
                                The perfect tool for freeform expression
                                Hold right click to spray in a large cone
                                Hold left click to spray in a small cone"""
				);
			});

			translationBuilder.add(AllItems.PRESSURE_WASHER, "Pressure Washer");
			translationBuilder.add("lore.street_art.pressure_washer",
					"""
                            The perfect tool for cleaning up freeform expression
                            Hold right click to clean in a horizontal line
                            Hold left click to clean in a vertical line"""
			);

			translationBuilder.add(AllItems.WATER_BALLOON, "Water Balloon");
			translationBuilder.add("lore.street_art.water_balloon",
					"Throw to wash away a small area of paint"
			);

			AllItems.PAINT_BALLOONS.forEach((color, item) -> {
				final String dyeName = WordUtils.capitalize(color.getName().replace("_", " "));
				translationBuilder.add(item, StringUtils.capitalize(dyeName + " Paint Balloon"));
				translationBuilder.add("lore.street_art." + dyeName + "_paint_balloon",
						"Throw to spread a small area of paint"
				);
			});

			translationBuilder.add(AllItems.RED_ROLLERBLADES, "Red Rollerblades");
			translationBuilder.add("lore.street_art.red_rollerblades",
					"""
							Why called rollerblade if no sword"""
			); // todo ran out of funny juice :(

			translationBuilder.add(AllItems.CREATIVE_PRESSURE_WASHER, "Creative Pressure Washer");
			translationBuilder.add("lore.street_art.creative_pressure_washer",
					"""
                            The superior tool for crushing freeform expression (Operator only)
                            R-Click a block to remove all paint from all faces in a radius
                            R-Click the air to reduce the radius
                            Sneak + R-Click in the air to increase the radius"""
			);

			translationBuilder.add(AllItems.SEALANT, "Sealant");
			translationBuilder.add("lore.street_art.sealant",
					"""
							Preserves your art for generations to come (Operator only)
							Hold R-Click and drag to define a region
							L-Click on a region to remove
							Spray paint within will not degrade over time"""
			);

			translationBuilder.add(AllItems.PERMIT_WAND, "Permit Wand");
			translationBuilder.add("lore.street_art.permit_wand",
					"""
							Sorry I got lazy (Operator only)
							Hold R-Click and drag to define a region
							L-Click on a region to remove
							Adventure mode players can modify paint on blocks within"""
			);

			translationBuilder.add(AllItems.DENY_WAND, "Deny Wand");
			translationBuilder.add("lore.street_art.deny_wand",
					"""
							Sorry I got lazy (Operator only)
							Hold R-Click and drag to define a region
							L-Click on a region to remove
							Only opped players can modify paint on blocks within"""
			);

			translationBuilder.add(AllItems.CREATIVE_TAB_KEY, "Street Art");

			translationBuilder.add("commands.street_art.clear.success", "Explodiated paint off of %s blocks");
			translationBuilder.add("commands.street_art.fill.success", "It's all over %s blocks");
			translationBuilder.add("commands.street_art.count.any_success", "Counted %s %s");
			translationBuilder.add("commands.street_art.count.color_success", "Counted %s %s with color %s");
		}
	}

	private static class StreetArtModelProvider extends FabricModelProvider {
		public StreetArtModelProvider(final FabricPackOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(final BlockModelGenerators blockModelGenerators) {

		}

		public static void generateDyedItemsNicely(final ItemModelGenerators itemModelGenerators, final Item item, final Identifier id) {
			final TextureMapping mapping = new TextureMapping().put(TextureSlot.LAYER0, new Material(id, false));
			final Identifier flatItem = ModelTemplates.FLAT_ITEM.create(id, mapping, itemModelGenerators.modelOutput);
			itemModelGenerators.itemModelOutput.accept(item, ItemModelUtils.plainModel(flatItem));
		}

		public static final ModelTemplate SPRAY_CAN_HAND = new ModelTemplate(
				Optional.of(StreetArt.id("item/spray_can/hand")),
				Optional.of("_spray_can"),
				TextureSlot.LAYER0, TextureSlot.PARTICLE
		);

		public static final ModelTemplate PRESSURE_WASHER_HAND = new ModelTemplate(
				Optional.of(StreetArt.id("item/pressure_washer")),
				Optional.empty(),
				TextureSlot.LAYER0, TextureSlot.PARTICLE
		);

		public static void generateSplitGuiHand(final ItemModelGenerators itemModelGenerators, final Item item,
												final Identifier guiLocation, final Identifier handLocation,
												final ModelTemplate handModel) {
			final TextureMapping flatMapping = new TextureMapping()
					.put(TextureSlot.LAYER0, new Material(guiLocation, false));
			final Identifier flatItem = ModelTemplates.FLAT_ITEM.create(guiLocation, flatMapping, itemModelGenerators.modelOutput);
			final ItemModel.Unbaked flat = ItemModelUtils.plainModel(flatItem);

			final TextureMapping handMapping = new TextureMapping()
					.put(TextureSlot.LAYER0, new Material(handLocation, false))
					.put(TextureSlot.PARTICLE, new Material(handLocation, false));
			final Identifier handItem = handModel.create(handLocation, handMapping, itemModelGenerators.modelOutput);
			final ItemModel.Unbaked hand = ItemModelUtils.plainModel(handItem);

			itemModelGenerators.itemModelOutput.accept(item, ItemModelGenerators.createFlatModelDispatch(
					flat, hand
			));
		}

		@Override
		public void generateItemModels(final ItemModelGenerators itemModelGenerators) {
			AllItems.SPRAY_CANS.forEach((color, item) -> {
				final Identifier baseGui = StreetArt.id("item/spray_can/gui/" + color.getName());
				final Identifier baseHand = StreetArt.id("item/spray_can/hand/" + color.getName());
				generateSplitGuiHand(itemModelGenerators, item, baseGui, baseHand, SPRAY_CAN_HAND);
			});
			AllItems.PAINT_BALLOONS.forEach((color, item) -> {
				final Identifier id = StreetArt.id("item/paint_balloon/" + color.getName());
				generateDyedItemsNicely(itemModelGenerators, item, id);
			});
			AllItems.ROLLERBLADES.forEach(item -> {
				itemModelGenerators.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
			});
			generateSplitGuiHand(itemModelGenerators, AllItems.PRESSURE_WASHER,
					StreetArt.id("item/pressure_washer/item"),
					StreetArt.id("item/pressure_washer/hand"),
					PRESSURE_WASHER_HAND);
			generateSplitGuiHand(itemModelGenerators, AllItems.CREATIVE_PRESSURE_WASHER,
					StreetArt.id("item/pressure_washer/creative_item"),
					StreetArt.id("item/pressure_washer/creative_hand"),
					PRESSURE_WASHER_HAND);
			itemModelGenerators.generateFlatItem(AllItems.WATER_BALLOON, ModelTemplates.FLAT_ITEM);
			itemModelGenerators.generateFlatItem(AllItems.SEALANT, ModelTemplates.FLAT_ITEM);
			itemModelGenerators.generateFlatItem(AllItems.PERMIT_WAND, ModelTemplates.FLAT_ITEM);
			itemModelGenerators.generateFlatItem(AllItems.DENY_WAND, ModelTemplates.FLAT_ITEM);
		}
	}

	private static class StreetArtTagProvider extends FabricTagsProvider.ItemTagsProvider {
		public StreetArtTagProvider(final FabricPackOutput output, final CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
			super(output, registryLookupFuture);
		}

		@Override
		protected void addTags(final HolderLookup.Provider registries) {
			final TagAppender<Item, Item> cans = this.valueLookupBuilder(AllTags.Items.SPRAY_CANS);
			final TagAppender<Item, Item> ballons = this.valueLookupBuilder(AllTags.Items.PAINT_BALLOONS);
			for (final DyeColor value : DyeColor.values()) {
				cans.add(AllItems.SPRAY_CANS.get(value));
				ballons.add(AllItems.PAINT_BALLOONS.get(value));
			}

			this.valueLookupBuilder(AllTags.Items.PAINT_CREATORS)
					.addTag(AllTags.Items.SPRAY_CANS)
					.addTag(AllTags.Items.PAINT_BALLOONS);
		}
	}

	private static class StreetArtRecipeProvider extends FabricRecipeProvider {
		protected StreetArtRecipeProvider(final FabricPackOutput output, final CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected RecipeProvider createRecipeProvider(final HolderLookup.Provider registries, final RecipeOutput output) {
			return new RecipeProvider(registries, output) {
				@Override
				public void buildRecipes() {

					for (int i = 0; i < dyesInOrder.size(); i++) {
						final Item dye = dyesInOrder.get(i);
						final DyeColor color = DyeColor.byId(i);
						sprayCan(this, this.output, AllItems.SPRAY_CANS.get(color), dye);
						paintBalloon(this, AllItems.PAINT_BALLOONS.get(color), dye)
								.unlockedBy("has_dye", this.has(dye))
								.group("street_art:paint_balloons")
								.save(this.output);
					}
					paintBalloon(this, AllItems.WATER_BALLOON, Items.WATER_BUCKET)
							.unlockedBy("has_paint_creator", this.has(AllTags.Items.PAINT_CREATORS))
							.save(this.output);

					this.shaped(RecipeCategory.DECORATIONS, AllItems.PRESSURE_WASHER)
							.define('I', ConventionalItemTags.IRON_INGOTS)
							.define('R', ConventionalItemTags.REDSTONE_DUSTS)
							.define('P', Items.PISTON)
							.define('B', Items.WATER_BUCKET)
							.pattern("BII")
							.pattern("PR ")
							.unlockedBy("has_paint_creator", this.has(AllTags.Items.PAINT_CREATORS))
							.save(this.output);
				}
			};
		}

		public static void sprayCan(final RecipeProvider prov, final RecipeOutput output,
									final ItemLike result, final ItemLike dye) {
			prov.shaped(RecipeCategory.DECORATIONS, result)
					.define('N', ConventionalItemTags.IRON_NUGGETS)
					.define('D', dye)
					.define('I', ConventionalItemTags.IRON_INGOTS)
					.pattern("N")
					.pattern("D")
					.pattern("I")
					.group("street_art:spray_can")
					.unlockedBy("has_needed_dye", prov.has(dye))
					.save(output);
		}

		public static ShapelessRecipeBuilder paintBalloon(final RecipeProvider prov, final ItemLike result, final ItemLike dye) {
			return prov.shapeless(RecipeCategory.DECORATIONS, result, 8)
					.requires(Items.DRIED_KELP)
					.requires(Items.PAPER)
					.requires(dye);
		}

		@Override
		public String getName() {
			return "StreetArtRecipeProvider";
		}
	}
}
