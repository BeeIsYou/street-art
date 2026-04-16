package com.streetart.client.mixin.rollerblades;

import com.streetart.client.rendering.rollerblades.LivingEntityRenderStateExtras;
import com.streetart.client.rendering.rollerblades.OverwrittenWalkAnimationState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void streetArt$appendState(final LivingEntity entity, final LivingEntityRenderState state, final float partialTicks, final CallbackInfo ci) {
        LivingEntityRenderStateExtras.extractSweep(state, (OverwrittenWalkAnimationState)entity.walkAnimation);
    }
}
