package com.streetart.client.mixin.rollerblades;

import com.streetart.schmoovement.RollerBlades;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "spawnSprintParticle", at = @At("HEAD"), cancellable = true)
    private void streetArt$noRollerbladeParticles(CallbackInfo ci) {
        if ((Object)this instanceof LivingEntity entity && RollerBlades.canRoll(entity)) {
            ci.cancel();
        }
    }
}
