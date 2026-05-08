package com.streetart;

import com.streetart.arealib.AreaLib;
import com.streetart.component.ChargeComponent;
import com.streetart.component.ColorComponent;
import com.streetart.component.RollerbladeComponent;
import com.streetart.component.paint_placer.PaintPlacerComponent;
import com.streetart.item.*;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class AllItems {
    public static final List<Item> ROLLERBLADES = new ArrayList<>();
    private static final UseEffects NONE = new UseEffects(true, false, 1);

    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), StreetArt.id("creative_tab")
    );

    public static Map<DyeColor, SprayCanItem> SPRAY_CANS = registerDyed("spray_can", SprayCanItem::new,
            dye -> new Item.Properties().stacksTo(1)
                    .component(AllDataComponents.PAINT_PLACER, PaintPlacerComponent.sprayCan(ColorComponent.fromDye(dye)))
                    .component(AllDataComponents.BUNDLE_STACK_SIZE_OVERRIDE, 16)
                    .component(DataComponents.USE_EFFECTS, NONE)
    );

    public static PressureWasherItem PRESSURE_WASHER = register("pressure_washer", PressureWasherItem::new,
            new Item.Properties().stacksTo(1)
                    .component(AllDataComponents.PAINT_PLACER, PaintPlacerComponent.pressureWasher())
                    .component(DataComponents.USE_EFFECTS, NONE)
    );

    public static PaintBalloonItem WATER_BALLOON = register("water_balloon", PaintBalloonItem::new,
            new Item.Properties().stacksTo(16).useCooldown(0.25f)
    );

    public static Map<DyeColor, PaintBalloonItem> PAINT_BALLOONS = registerDyed("paint_balloon", PaintBalloonItem::new,
            dye -> new Item.Properties().stacksTo(16).useCooldown(0.25f)
                    .component(AllDataComponents.COLOR, ColorComponent.fromDye(dye))
    );

    public static Item RED_ROLLERBLADES = registerRollerblades("red_rollerblades");

    public static Item GREEN_ROLLERBLADES = registerRollerblades("green_rollerblades", properties -> {
        Optional<Holder.Reference<Attribute>> bounciness = BuiltInRegistries.ATTRIBUTE.get(
                Identifier.fromNamespaceAndPath("slime_time", "bounciness")
        );
        bounciness.ifPresent(attributeHolder -> properties.attributes(ItemAttributeModifiers.builder()
                .add(
                        attributeHolder,
                        new AttributeModifier(StreetArt.id("bouncy_skates"), 1, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.FEET
                ).build()));
    });

    public static Item TAPE_RECORDER = register("tape_recorder", TapeRecorderItem::new,
            new Item.Properties().stacksTo(1).useCooldown(1f));

    public static Item BLANK_TRACK = register("blank_track", Item::new,
            new Item.Properties());

    public static Item TRACK = register("track", Item::new,
            new Item.Properties());

//    public static Item SWAG = register("swag", Item::new,
//            new Item.Properties().equippable(EquipmentSlot.HEAD));

    public static CreativePressureWasherItem CREATIVE_PRESSURE_WASHER = register("creative_pressure_washer", CreativePressureWasherItem::new,
            new Item.Properties().stacksTo(1)
                    .component(AllDataComponents.CHARGE, new ChargeComponent(0, 3))
                    .component(DataComponents.USE_EFFECTS, NONE)
    );

    public static AreaModifierItem SEALANT = register("sealant", AreaModifierItem.forType(AreaLib.Type.NO_DECAY),
            new Item.Properties().stacksTo(1)
                    .component(DataComponents.USE_EFFECTS, NONE)
    );

    public static AreaModifierItem PERMIT_WAND = register("permit_wand", AreaModifierItem.forType(AreaLib.Type.MODIFYING_ALLOWED),
            new Item.Properties().stacksTo(1)
                    .component(DataComponents.USE_EFFECTS, NONE)
    );

    public static AreaModifierItem DENY_WAND = register("deny_wand", AreaModifierItem.forType(AreaLib.Type.PROTECTED),
            new Item.Properties().stacksTo(1)
                    .component(DataComponents.USE_EFFECTS, NONE)
    );

    public static final CreativeModeTab CREATIVE_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(SPRAY_CANS.get(DyeColor.RED)))
            .title(Component.translatable("key.category.street_art"))
            .displayItems((parameters, output) -> {
                for (DyeColor color : DyeColor.values()) {
                    ItemStack stack = new ItemStack(SPRAY_CANS.get(color));
                    output.accept(stack);
                }
                output.accept(WATER_BALLOON);
                for (DyeColor color : DyeColor.values()) {
                    if (color != DyeColor.WHITE) { // i know what you are
                        ItemStack stack = new ItemStack(PAINT_BALLOONS.get(color));
                        output.accept(stack);
                    }
                }
                output.accept(PRESSURE_WASHER);
                ROLLERBLADES.forEach(output::accept);
                output.accept(AllItems.TAPE_RECORDER);
                output.accept(AllItems.BLANK_TRACK);
            }).build();

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.OP_BLOCKS).register(content -> {
            content.accept(CREATIVE_PRESSURE_WASHER);
            content.accept(SEALANT);
            content.accept(PERMIT_WAND);
            content.accept(DENY_WAND);
        });
        DispenserBlock.registerProjectileBehavior(WATER_BALLOON);
        PAINT_BALLOONS.values().forEach(DispenserBlock::registerProjectileBehavior);
    }

    private static <T extends Item> EnumMap<DyeColor, T> registerDyed(final String baseName,
                                                                      final Function<Item.Properties, T> factory,
                                                                      final Function<DyeColor, Item.Properties> properties) {
        final EnumMap<DyeColor, T> map = new EnumMap<>(DyeColor.class);

        for (final DyeColor value : DyeColor.values()) {
            map.put(value, register(value.getName() + "_" + baseName, factory, properties.apply(value)));
        }

        return map;
    }

    private static <T extends Item> T register(final String name, final Function<Item.Properties, T> factory, final Item.Properties properties) {
        final ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, StreetArt.id(name));

        final T item = factory.apply(properties.setId(key));

        Registry.register(BuiltInRegistries.ITEM, key, item);

        return item;
    }

    private static Item registerRollerblades(final String name) {
        return registerRollerblades(name, _ -> {});
    }

    private static Item registerRollerblades(final String name, Consumer<Item.Properties> propertiesModifier) {
        Item.Properties properties = new Item.Properties().stacksTo(1)
                .equippable(EquipmentSlot.FEET)
                .component(AllDataComponents.ROLLER_BLADES, RollerbladeComponent.streetArt(name));
        propertiesModifier.accept(properties);
        final Item item = register(name, Item::new, properties);
        ROLLERBLADES.add(item);
        return item;
    }
}
