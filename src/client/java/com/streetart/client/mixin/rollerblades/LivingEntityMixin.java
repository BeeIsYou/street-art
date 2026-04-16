package com.streetart.client.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.streetart.client.rendering.rollerblades.OverwrittenWalkAnimationState;
import com.streetart.schmoovement.RollerBlades;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /**
     * @author BeeIsYou
     * @reason The walk animation state does not nicely support variable positionScale and is annoyingly inextensible. If this actually causes compat issues poke me to make a billion mixins
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "Lnet/minecraft/world/entity/WalkAnimationState;"))
    private WalkAnimationState streetArt$overwriteWalkAnimationClass() {
        return new OverwrittenWalkAnimationState();
    }

    @WrapOperation(method = "updateWalkAnimation", at = @At(value = "INVOKE", target = "update"))
    private void streetArt$majesticLegs(final WalkAnimationState instance, final float speed, final float factor, final float positionScale, final Operation<Void> operation) {
        if (RollerBlades.canRoll((LivingEntity)(Object)this)) {
            final Vector4f animationModifiers = RollerBlades.getAnimationModifier((LivingEntity)(Object)this);
            operation.call(instance,
                    speed * animationModifiers.x,
                    factor * animationModifiers.y,
                    positionScale * animationModifiers.z
            );
            ((OverwrittenWalkAnimationState)instance).setLegSweep(animationModifiers.w);
        } else {
            operation.call(instance, speed, factor, positionScale);
            ((OverwrittenWalkAnimationState)instance).setLegSweep(1);
        }
    }

    @WrapOperation(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z"))
    private boolean streetArt$noRollerbladeSprintJump(final LivingEntity instance, final Operation<Boolean> operation) {
        if (RollerBlades.canRoll(instance)) {
            return false;
        }
        return operation.call(instance);
    }
}
