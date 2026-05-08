package com.streetart.mixin;

import com.streetart.AllDataComponents;
import com.streetart.component.paint_placer.PaintPlacerComponent;
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
    private void streetArt$noAttackSwing(final ItemStack itemStack, final int tolerance, final CallbackInfoReturnable<Boolean> cir) {
        final PaintPlacerComponent paintPlacer = itemStack.get(AllDataComponents.PAINT_PLACER);
        if (paintPlacer != null && paintPlacer.leftClick().isPresent()) {
            cir.setReturnValue(true);
        }
    }
}
