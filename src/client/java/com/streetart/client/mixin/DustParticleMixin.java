package com.streetart.client.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.client.particle.DustParticleBase;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.DustParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DustParticle.class)
public abstract class DustParticleMixin extends DustParticleBase<DustParticleOptions> {
    protected DustParticleMixin(ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, DustParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, xAux, yAux, zAux, options, sprites);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void streetArt$velocity(
            final ClientLevel level,
            final double x,
            final double y,
            final double z,
            final double xAux,
            final double yAux,
            final double zAux,
            final DustParticleOptions options,
            final SpriteSet sprites,
            CallbackInfo ci
    ) {
        this.setParticleSpeed(xAux, yAux, zAux);
    }
}
