package com.streetart.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.streetart.misc.OverwrittenWalkAnimationState;
import com.streetart.mixinterface.IHasRollerbladeController;
import com.streetart.schmoovement.RollerbladeController;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.level.Level;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IHasRollerbladeController {
    @Unique
    private RollerbladeController controller = new RollerbladeController((LivingEntity)(Object)this);

    public RollerbladeController getController() {
        return this.controller;
    }

    protected LivingEntityMixin(final EntityType<? extends LivingEntity> type, final Level level) {
        super(type, level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkRolling(final CallbackInfo ci) {
        this.controller.tickActive();
    }

    @ModifyVariable(method = "travelInAir", at = @At(value = "STORE", ordinal = 0))
    private float streetArt$rollerBladeBlockFriction(final float original) {
        if (this.controller.isActive()) {
            return (float) this.controller.getBlockFriction(original);
        }
        return original;
    }

    @ModifyConstant(method = "travelInAir", constant = @Constant(floatValue = 0.91f))
    private float streetArt$rollerBladeGlobalFriction(final float original) {
        if (this.controller.isActive()) {
            return 1;
        }
        return original;
    }

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
        if (this.controller.isActive()) {
            final Vector4f animationModifiers = this.controller.getAnimationModifiers();
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
        if (this.controller.isActive()) {
            return false;
        }
        return operation.call(instance);
    }

    @Override
    protected void spawnSprintParticle() {
        if (!this.controller.isActive()) {
            super.spawnSprintParticle();
        }
    }
}
