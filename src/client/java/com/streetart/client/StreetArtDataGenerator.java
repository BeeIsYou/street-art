package com.streetart.client;

import com.streetart.AllItems;
import com.streetart.AllTags;
import com.streetart.StreetArt;
import com.streetart.component.TapeRecorderContents;
import com.streetart.recipe.TrackDuplicateRecipe;
import com.streetart.recipe.TrackDyeRecipe;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.*;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
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

			translationBuilder.add(AllItems.TAPE_RECORDER, "Tape Recorder");
			translationBuilder.add("lore.street_art.tape_recorder",
					"""
							Insert a blank tape and right click to record your path
							Right click again to finish recording
							Share your routes and slick tricks"""
			);

			translationBuilder.add(AllItems.BLANK_TRACK, "Blank Track");
			translationBuilder.add("lore.street_art.blank_track",
					"""
							Insert into a tape recorder to record a path"""
			);

			translationBuilder.add(AllItems.TRACK, "Track");
			translationBuilder.add("lore.street_art.track",
					"""
							A recorded path
							Insert into a tape recorder to view it"""
			);

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

			translationBuilder.add(AllTags.Items.SPRAY_CANS, "Spray Cans");
			translationBuilder.add(AllTags.Items.PAINT_BALLOONS, "Paint Balloons");
			translationBuilder.add(AllTags.Items.PAINT_CREATORS, "Paint Creators");

			translationBuilder.add("street_art.tape_recorder.message.start", "Track Recording Started");
			translationBuilder.add("street_art.tape_recorder.message.cancel", "Track Recording Cancelled");
			translationBuilder.add("street_art.tape_recorder.message.success", "Track Successfully Recorded");
			translationBuilder.add("street_art.tape_recorder.message.failure", "Track Recording Failed :(");

			translationBuilder.add("street_art.track.author", "Recorded by: %s");
			translationBuilder.add("street_art.track.duration.seconds", "Duration: %ss");
			translationBuilder.add("street_art.track.duration.minutes_seconds", "Duration: %s:%s");
			translationBuilder.add("street_art.track.start_position", "Starts at: %s %s %s");

			translationBuilder.add("commands.street_art.clear.success", "Explodiated paint off of %s blocks");
			translationBuilder.add("commands.street_art.fill.success", "It's all over %s blocks");
			translationBuilder.add("commands.street_art.count.any_success", "Counted %s %s");
			translationBuilder.add("commands.street_art.count.color_success", "Counted %s %s with color %s");

			translationBuilder.add("gamerule.category.street_art.game_rules", "Street Art");
			translationBuilder.add("gamerule.street_art.adventure_painting", "Allow Adventure Players to Paint");
			translationBuilder.add("gamerule.street_art.second_layer_adventure_painting", "Allow Adventure Players to Paint on the secondary layer");
			translationBuilder.add("gamerule.street_art.non_players_adventure", "Non-Player Painting counts as Adventure");
			translationBuilder.add("gamerule.street_art.random_decay_speed", "Random Paint Decay Speed");
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

		public static void generateTrack(final ItemModelGenerators itemModelGenerators, final Item item,
										 final Identifier target, final Identifier base, final Identifier colorA, final Identifier colorB) {
			itemModelGenerators.generateLayeredItem(target, new Material(base), new Material(colorA), new Material(colorB));
			final ItemModel.Unbaked model = ItemModelUtils.tintedModel(
					target,
					new Constant(-1),
					new TrackTintSource(DyeColor.RED.getTextureDiffuseColor(), true),
					new TrackTintSource(DyeColor.ORANGE.getTextureDiffuseColor(), false));

			itemModelGenerators.itemModelOutput.accept(item, model);
		}

		public static void generateTapeRecorder(final ItemModelGenerators itemModelGenerators, final Item item,
                                                final Identifier emptyName, final Identifier blankName, final Identifier trackName, final Identifier colorA, final Identifier colorB) {
			final Identifier emptyModel = ModelTemplates.FLAT_ITEM.create(emptyName, TextureMapping.layer0(new Material(emptyName, false)), itemModelGenerators.modelOutput);
			final Identifier blankModel = ModelTemplates.FLAT_ITEM.create(blankName, TextureMapping.layer0(new Material(blankName, false)), itemModelGenerators.modelOutput);
			itemModelGenerators.generateLayeredItem(trackName, new Material(trackName), new Material(colorA), new Material(colorB));
			final ItemModel.Unbaked empty = ItemModelUtils.plainModel(emptyModel);
			final ItemModel.Unbaked blank = ItemModelUtils.plainModel(blankModel);
			final ItemModel.Unbaked track = ItemModelUtils.tintedModel(
					trackName,
					new Constant(-1),
					new TrackTintSource(DyeColor.RED.getTextureDiffuseColor(), true),
					new TrackTintSource(DyeColor.ORANGE.getTextureDiffuseColor(), false));
			final ItemModel.Unbaked cases = ItemModelUtils.select(
					new TapeRecorderContentsPropery(TapeRecorderContents.State.EMPTY),
					new SelectItemModel.SwitchCase<>(List.of(TapeRecorderContents.State.EMPTY), empty),
					new SelectItemModel.SwitchCase<>(List.of(TapeRecorderContents.State.BLANK), blank),
					new SelectItemModel.SwitchCase<>(List.of(TapeRecorderContents.State.RECORDED), track)
			);
			itemModelGenerators.itemModelOutput.accept(item, cases);
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
			generateTapeRecorder(itemModelGenerators, AllItems.TAPE_RECORDER,
					StreetArt.id("item/tape_recorder/empty"),
					StreetArt.id("item/tape_recorder/blank"),
					StreetArt.id("item/tape_recorder/base"),
					StreetArt.id("item/tape_recorder/color_a"),
					StreetArt.id("item/tape_recorder/color_b"));
			itemModelGenerators.generateFlatItem(AllItems.BLANK_TRACK, ModelTemplates.FLAT_ITEM);
			generateTrack(itemModelGenerators, AllItems.TRACK, StreetArt.id("item/track"),
					StreetArt.id("item/track/base"),
					StreetArt.id("item/track/color_a"),
					StreetArt.id("item/track/color_b"));
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

			final TagAppender<Item, Item> rollerblades = this.valueLookupBuilder(AllTags.Items.ROLLERBLADES);
			for (final Item item : AllItems.ROLLERBLADES) {
				rollerblades.add(item);
			}
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

					this.shaped(RecipeCategory.MISC, AllItems.BLANK_TRACK, 4)
							.define('P', Items.PAPER)
							.define('K', Items.DRIED_KELP)
							.define('I', ConventionalItemTags.IRON_INGOTS)
							.define('D', ConventionalItemTags.REDSTONE_DUSTS)
							.pattern(" P ")
							.pattern("KIK")
							.pattern(" D ")
							.unlockedBy("has_rollerblades", this.has(AllTags.Items.ROLLERBLADES))
							.save(this.output);

					this.shapeless(RecipeCategory.MISC, AllItems.BLANK_TRACK)
							.requires(AllItems.TRACK)
							.unlockedBy("has_track", this.has(AllItems.TRACK))
							.save(this.output, "street_art:track_clearing");

					trackDuplicate(this.output, Ingredient.of(AllItems.TRACK), Ingredient.of(AllItems.BLANK_TRACK),
							"street_art:copy_track_onto_blank_track");
					trackDye(this.output, Ingredient.of(AllItems.TRACK), this.tag(ItemTags.DYES),
							"street_art:dye_track");

					this.shaped(RecipeCategory.MISC, AllItems.TAPE_RECORDER)
							.define('I', ConventionalItemTags.IRON_INGOTS)
							.define('G', ConventionalItemTags.GLASS_PANES)
							.define('B', ItemTags.STONE_BUTTONS)
							.pattern(" I ")
							.pattern("IGI")
							.pattern("BIB")
							.unlockedBy("has_blank_track", this.has(AllItems.BLANK_TRACK))
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

		public static void trackDuplicate(final RecipeOutput output, final Ingredient copyFrom, final Ingredient copyOnto, final String name) {
			SpecialRecipeBuilder.special(() -> new TrackDuplicateRecipe(copyFrom, copyOnto))
					.save(output, name);
		}

		public static void trackDye(final RecipeOutput output, final Ingredient target, final Ingredient dye, final String name) {
			SpecialRecipeBuilder.special(() -> new TrackDyeRecipe(target, dye))
					.save(output, name);
		}

		@Override
		public String getName() {
			return "StreetArtRecipeProvider";
		}
	}
}
