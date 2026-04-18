package com.streetart.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.streetart.mixinterface.IHasRollerbladeController;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin implements IHasRollerbladeController {
    @WrapOperation(method = "isStayingOnGroundSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isShiftKeyDown()Z"))
    private boolean streetArt$noRollerbladeEdgeProtection(final Player instance, final Operation<Boolean> operation) {
        final boolean res = operation.call(instance);
        if (this.getController().isActive()) {
            return false;
        }
        return res;
    }
}
