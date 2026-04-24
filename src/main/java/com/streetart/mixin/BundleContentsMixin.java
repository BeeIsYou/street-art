package com.streetart.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.streetart.AllDataComponents;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BundleContents.class)
public class BundleContentsMixin {
    @WrapOperation(method = "getWeight", at = @At(value = "INVOKE", target = "getMaxStackSize"))
    private static int streetArt$weightOverride(final ItemInstance instance, final Operation<Integer> operation) {
        final Integer override = instance.get(AllDataComponents.BUNDLE_STACK_SIZE_OVERRIDE);
        if (override != null) {
            return override;
        }
        return operation.call(instance);
    }
}
