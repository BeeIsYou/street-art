package com.streetart;

import com.streetart.arealib.AreaLib;
import com.streetart.component.ChargeComponent;
import com.streetart.component.ColorComponent;
import com.streetart.item.*;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class AllItems {
    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), StreetArt.id("creative_tab")
    );

    public static SprayCanItem SPRAY_CAN = register("spray_can", SprayCanItem::new,
            new Item.Properties().stacksTo(1)
                    .component(AllDataComponents.COLOR, ColorComponent.RED)
    );

    public static PressureWasherItem PRESSURE_WASHER = register("pressure_washer", PressureWasherItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static PaintBalloonItem WATER_BALLOON = register("water_balloon", PaintBalloonItem::new,
            new Item.Properties().stacksTo(16).useCooldown(0.5f)
    );

    public static PaintBalloonItem PAINT_BALLOON = register("paint_balloon", PaintBalloonItem::new,
            new Item.Properties().stacksTo(16).useCooldown(0.5f)
                    .component(AllDataComponents.COLOR, ColorComponent.BLUE)
    );

    public static CreativePressureWasherItem CREATIVE_PRESSURE_WASHER = register("creative_pressure_washer", CreativePressureWasherItem::new,
            new Item.Properties().stacksTo(1)
                    .component(AllDataComponents.CHARGE, new ChargeComponent(0, 3))
    );

    public static AreaModifierItem SEALANT = register("sealant", AreaModifierItem.forType(AreaLib.Type.NO_DECAY),
            new Item.Properties().stacksTo(1)
    );

    public static AreaModifierItem PERMIT_WAND = register("permit_wand", AreaModifierItem.forType(AreaLib.Type.MODIFYING_ALLOWED),
            new Item.Properties().stacksTo(1)
    );

    public static AreaModifierItem DENY_WAND = register("deny_wand", AreaModifierItem.forType(AreaLib.Type.PROTECTED),
            new Item.Properties().stacksTo(1)
    );

    public static final CreativeModeTab CREATIVE_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(SPRAY_CAN))
            .title(Component.translatable("key.category.street_art"))
            .displayItems((parameters, output) -> {
                for (ColorComponent color : ColorComponent.values()) {
                    if (color != ColorComponent.CLEAR) {
                        ItemStack stack = new ItemStack(SPRAY_CAN);
                        stack.set(AllDataComponents.COLOR, color);
                        output.accept(stack);
                    }
                }
                output.accept(WATER_BALLOON);
                for (ColorComponent color : ColorComponent.values()) {
                    if (color != ColorComponent.CLEAR && color != ColorComponent.WHITE) { // i know what you are
                        ItemStack stack = new ItemStack(PAINT_BALLOON);
                        stack.set(AllDataComponents.COLOR, color);
                        output.accept(stack);
                    }
                }
                output.accept(PRESSURE_WASHER);
            }).build();

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.OP_BLOCKS).register(content -> {
            content.accept(CREATIVE_PRESSURE_WASHER);
            content.accept(SEALANT);
            content.accept(PERMIT_WAND);
            content.accept(DENY_WAND);
        });
    }

    private static <T extends Item> T register(final String name, final Function<Item.Properties, T> factory, final Item.Properties properties) {
        final ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, StreetArt.id(name));

        final T item = factory.apply(properties.setId(key));

        Registry.register(BuiltInRegistries.ITEM, key, item);

        return item;
    }
}
