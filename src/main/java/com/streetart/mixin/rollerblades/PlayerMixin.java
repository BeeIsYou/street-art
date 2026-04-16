package com.streetart.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.streetart.schmoovement.RollerBlades;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @WrapOperation(method = "isStayingOnGroundSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isShiftKeyDown()Z"))
    private boolean streetArt$noRollerbladeEdgeProtection(final Player instance, final Operation<Boolean> operation) {
        final boolean res = operation.call(instance);
        if (RollerBlades.canRoll(instance)) {
            return false;
        }
        return res;
    }
}
