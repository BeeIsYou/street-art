package com.streetart;

import com.streetart.component.ChargeComponent;
import com.streetart.item.PaintBalloonItem;
import com.streetart.item.PressureWasherItem;
import com.streetart.item.SprayCanItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.function.Function;

public class AllItems {
    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), StreetArt.id("creative_tab")
    );

    public static SprayCanItem SPRAY_CAN = register("spray_can", SprayCanItem::new,
            new Item.Properties().stacksTo(1).component(DataComponents.DYED_COLOR, new DyedItemColor(DyeColor.RED.getTextureDiffuseColor()))
    );

    public static PaintBalloonItem WATER_BALLOON = register("water_balloon", PaintBalloonItem::new,
            new Item.Properties().stacksTo(16).useCooldown(0.5f)
    );

    public static PaintBalloonItem PAINT_BALLOON = register("paint_balloon", PaintBalloonItem::new,
            new Item.Properties().stacksTo(16).useCooldown(0.5f).component(DataComponents.DYED_COLOR, new DyedItemColor(DyeColor.BLUE.getTextureDiffuseColor()))
    );

    public static PressureWasherItem PRESSURE_WASHER = register("pressure_washer", PressureWasherItem::new,
            new Item.Properties().stacksTo(1).component(AllDataComponents.CHARGE, new ChargeComponent(0, 3))
    );

    public static final CreativeModeTab CREATIVE_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(SPRAY_CAN))
            .title(Component.translatable("key.category.street_art"))
            .displayItems((parameters, output) -> {
                for (DyeColor color : DyeColor.values()) {
                    ItemStack stack = new ItemStack(SPRAY_CAN);
                    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getTextureDiffuseColor()));
                    output.accept(stack);
                }
                output.accept(WATER_BALLOON);
                for (DyeColor color : DyeColor.values()) {
                    if (color != DyeColor.WHITE) { // i know what you are
                        ItemStack stack = new ItemStack(PAINT_BALLOON);
                        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getTextureDiffuseColor()));
                        output.accept(stack);
                    }
                }
            }).build();

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.OP_BLOCKS).register(content -> {
            content.accept(PRESSURE_WASHER);
        });
    }

    private static <T extends Item> T register(final String name, final Function<Item.Properties, T> factory, final Item.Properties properties) {
        final ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, StreetArt.id(name));

        final T item = factory.apply(properties.setId(key));

        Registry.register(BuiltInRegistries.ITEM, key, item);

        return item;
    }
}
