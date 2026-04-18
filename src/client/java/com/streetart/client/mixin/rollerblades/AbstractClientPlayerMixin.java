package com.streetart.client.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.authlib.GameProfile;
import com.streetart.mixinterface.IHasRollerbladeController;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements IHasRollerbladeController {
    public AbstractClientPlayerMixin(final Level level, final GameProfile gameProfile) {
        super(level, gameProfile);
    }

    // the amplitude of view bob
    @WrapOperation(method = "updateBob", at = @At(value = "INVOKE", target = "updateBob"))
    private void streetArt$limitBob(final ClientAvatarState instance, final float value, final Operation<Void> operation) {
        if (this.getController().isActive()) {
            if (this.xxa == 0 && this.zza == 0) {
                operation.call(instance, 0f);
            } else {
                operation.call(instance, value * 0.5f);
            }
        } else {
            operation.call(instance, value);
        }
    }

    // the effective "frequency" of view bob. less distance -> slower bob animation
    @WrapOperation(method = "addWalkedDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/ClientAvatarState;addWalkDistance(F)V"))
    private void streetArt$slowBob(final ClientAvatarState instance, final float value, final Operation<Void> operation) {
        if (this.getController().isActive()) {
            if (this.xxa == 0 && this.zza == 0) {
                operation.call(instance, 0f);
            } else {
                operation.call(instance, value * 0.25f);
            }
        } else {
            operation.call(instance, value);
        }
    }

    @WrapOperation(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "getAttributeValue"))
    private double streetArt$useRollerbladeFov(final AbstractClientPlayer instance, final Holder<Attribute> attribute,
                                               final Operation<Double> operation, @Local(ordinal = 2) LocalFloatRef walkingSpeed) {
        double movementSpeed = operation.call(instance, attribute);
        if (this.getController().isActive()) {
            return this.getController().getFovScalar(walkingSpeed.get(), movementSpeed) * movementSpeed;
        }
        return movementSpeed;
    }
}
