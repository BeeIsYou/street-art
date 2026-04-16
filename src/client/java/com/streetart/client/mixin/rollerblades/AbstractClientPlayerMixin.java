package com.streetart.client.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import com.streetart.schmoovement.RollerBlades;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {
    public AbstractClientPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @WrapOperation(method = "updateBob", at = @At(value = "INVOKE", target = "updateBob"))
    private void streetArt$limitBob(final ClientAvatarState instance, final float value, final Operation<Void> operation) {
        if (RollerBlades.canRoll(this)) {
            operation.call(instance, value * 0.25f);
        } else {
            operation.call(instance, value);
        }
    }
}
