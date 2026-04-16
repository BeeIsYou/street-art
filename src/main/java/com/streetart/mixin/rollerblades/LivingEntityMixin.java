package com.streetart.mixin.rollerblades;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.streetart.schmoovement.RollerBlades;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
}
