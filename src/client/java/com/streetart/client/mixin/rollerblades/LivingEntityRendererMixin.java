package com.streetart.client.mixin.rollerblades;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.streetart.AllDataComponents;
import com.streetart.client.rendering.rollerblades.LivingEntityRenderStateExtras;
import com.streetart.misc.OverwrittenWalkAnimationState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Shadow
    EntityModel<?> model;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void streetArt$appendState(final LivingEntity entity, final LivingEntityRenderState state, final float partialTicks, final CallbackInfo ci) {
        LivingEntityRenderStateExtras.extractSweep(state, (OverwrittenWalkAnimationState)entity.walkAnimation);
    }

    @Inject(method = "submit", at = @At(value = "INVOKE", ordinal = 1, target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void streetArt$fiveFootEleven(final CallbackInfo ci, @Local(argsOnly = true) final LivingEntityRenderState state, @Local(argsOnly = true) final PoseStack poseStack) {
        if (state instanceof final HumanoidRenderState humanoidState) {
            if (humanoidState.feetEquipment.has(AllDataComponents.ROLLER_BLADES)) {
                if (humanoidState instanceof ArmorStandRenderState) {
                    poseStack.translate(0, -4 / 16f, 0);
                } else {
                    poseStack.translate(0, -3 / 16f, 0);
                }
            }
        }
    }
}
