package com.streetart.client.mixin.rollerblades;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.streetart.client.rendering.rollerblades.LivingEntityRenderStateExtras;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Redirect(method = "setupAnim", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(D)F", ordinal = 0))
    private float streetArt$calculateAndUseSweep1(final double i,
                                                  @Local(argsOnly = true) final HumanoidRenderState state,
                                                  @Share("cosResult") final LocalFloatRef cosResult) {
        float cos = Mth.cos(i);
        final float sweep = LivingEntityRenderStateExtras.getSweep(state);
        if (sweep != 1) {
            // a higher sweep value means limbs will linger at the extremes more, and go through 0 faster
            if (cos > 0) {
                cos = (float) (1 - Math.pow(1 - cos, sweep));
            } else {
                cos = (float) (Math.pow(1 + cos, sweep) - 1);
            }
        }

        cosResult.set(cos);
        return cos;
    }

    @Redirect(method = "setupAnim", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(D)F", ordinal = 1))
    private float streetArt$useSweep2(final double i, @Share("cosResult") final LocalFloatRef cosResult) {
        return -cosResult.get();
    }
    @Redirect(method = "setupAnim", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(D)F", ordinal = 2))
    private float streetArt$useSweep3(final double i, @Share("cosResult") final LocalFloatRef cosResult) {
        return -cosResult.get();
    }
    @Redirect(method = "setupAnim", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(D)F", ordinal = 3))
    private float streetArt$useSweep4(final double i, @Share("cosResult") final LocalFloatRef cosResult) {
        return cosResult.get();
    }
}
