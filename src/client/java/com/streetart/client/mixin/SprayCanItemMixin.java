package com.streetart.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.streetart.SprayCanItem;
import com.streetart.client.StreetArtClient;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// todo delete delete kill explode
@Mixin(SprayCanItem.class)
public class SprayCanItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"))
    private void evil(CallbackInfoReturnable<InteractionResult> cir, @Local UseOnContext useOnContext) {
//        if (useOnContext.getLevel().isClientSide()) { //I don't think we can actually remove this as we don't have access to client classes in main project
//            StreetArtClient.textureManager.getOrNew(
//                    useOnContext.getClickedPos(),
//                    useOnContext.getClickLocation(),
//                    useOnContext.getClickedFace()
//            );
//        }
    }
}
