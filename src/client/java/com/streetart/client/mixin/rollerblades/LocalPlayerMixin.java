package com.streetart.client.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.streetart.mixinterface.IHasRollerbladeController;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "isSwimming", ordinal = 2))
    private boolean streetArt$rollerbladeRestrictFlight(LocalPlayer instance, Operation<Boolean> operation) {
        if (this instanceof IHasRollerbladeController controller && controller.getController().isActive() &&
                !controller.getController().currentMovement.mayFly()) {
            // if the player is swimming, they are not allowed to enter creative flight. ez
            return true;
        }
        return operation.call(instance);
    }
}
