package com.streetart.client.mixin;

import com.streetart.client.manager.SpraySessionManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "turn", at = @At("TAIL"))
	private void streetArt$spraySessionSnapshot(double xo, double yo, CallbackInfo ci) {
		if ((Object) this instanceof LocalPlayer player) {
			SpraySessionManager.takeSnapshot(player);
		}
    }
}
