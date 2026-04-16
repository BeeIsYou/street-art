package com.streetart.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.streetart.misc.OverwrittenWalkAnimationState;
import com.streetart.schmoovement.RollerBlades;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    protected LivingEntityMixin(final EntityType<? extends LivingEntity> type, final Level level) {
        super(type, level);
    }

    @ModifyVariable(method = "travelInAir", at = @At(value = "STORE", ordinal = 0))
    private float streetArt$rollerBladeBlockFriction(final float original, @Share("rollin") final LocalBooleanRef rollin) {
        rollin.set(RollerBlades.canRoll((LivingEntity)(Object)this));
        if (rollin.get()) {
            return (float)RollerBlades.getBlockFriction(original, (LivingEntity)(Object)this);
        }
        return original;
    }

    @ModifyConstant(method = "travelInAir", constant = @Constant(floatValue = 0.91f))
    private float streetArt$rollerBladeGlobalFriction(final float original, @Share("rollin") final LocalBooleanRef rollin) {
        if (rollin.get()) {
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
    
    @WrapOperation(method = "aiStep", at = @At(value = "NEW", target = "Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 streetArt$modifyInputVector(double xxa, double yya, double zza, Operation<Vec3> operation) {
        if (RollerBlades.canRoll((LivingEntity)(Object)this)) {
            Vec2 move = RollerBlades.handleInput(new Vec2((float) xxa, (float) zza), (LivingEntity)(Object)this);
            xxa = move.x;
            zza = move.y;
        }
        return operation.call(xxa, yya, zza);
    }
    
}
