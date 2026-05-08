package com.streetart.client.mixin.rollerblades;

import com.streetart.AllDataComponents;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.model.object.armorstand.ArmorStandModel;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandModel.class)
public abstract class ArmorStandModelMixin extends ArmorStandArmorModel {
    @Shadow
    @Final
    private ModelPart basePlate;

    public ArmorStandModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;)V", at = @At("TAIL"))
    private void streetArt$offsetBasePlate(ArmorStandRenderState state, CallbackInfo ci) {
        if (state.feetEquipment.has(AllDataComponents.ROLLER_BLADES)) {
            this.basePlate.y += 3 + 1;
        }
    }
}
