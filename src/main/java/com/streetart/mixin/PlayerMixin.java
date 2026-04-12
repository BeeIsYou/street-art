package com.streetart.mixin;

import com.streetart.AllItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "Lnet/minecraft/world/entity/player/Player;cannotAttackWithItem(Lnet/minecraft/world/item/ItemStack;I)Z",
        at = @At("HEAD"), cancellable = true
    )
    private void streetArt$noCanSwing(final ItemStack itemStack, final int tolerance, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.is(AllItems.SPRAY_CAN)) {
            cir.setReturnValue(true);
        }
    }
}
