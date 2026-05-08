package com.streetart.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.streetart.AllDataComponents;
import com.streetart.component.paint_placer.PaintPlacerComponent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    LocalPlayer player;
    @Shadow
    Options options;

    @Unique
    private boolean wasDown;

    @WrapOperation(method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V")
    )
    private void streetArt$overrideAttack(final Minecraft instance, final boolean value, final Operation<Void> operation) {
        final ItemStack mainhand = instance.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (value) {
            final PaintPlacerComponent paintPlacer = mainhand.get(AllDataComponents.PAINT_PLACER);
            if (paintPlacer != null && paintPlacer.leftClick().isPresent()) {
                instance.gameMode.useItem(instance.player, InteractionHand.MAIN_HAND);
                operation.call(instance, false);
                this.wasDown = true;
                return;
            }
        }
        this.wasDown = value;
        operation.call(instance, value);
    }

    @WrapOperation(method = "handleKeybinds",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/KeyMapping;isDown()Z")
    )
    private boolean streetArt$keepUsingCan(final KeyMapping instance, final Operation<Boolean> operation) {
        final ItemStack mainhand = this.player.getMainHandItem();
        if (mainhand.has(AllDataComponents.PAINT_PLACER)) {
            return operation.call(instance) && !this.options.keyAttack.isDown();
        }
        return operation.call(instance);
    }
}
