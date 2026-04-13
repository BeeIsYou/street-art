package com.streetart.client.mixin;

import com.streetart.client.ParticleThrower;
import com.streetart.item.PressureWasherItem;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PressureWasherItem.class)
public class PressureWasherMixin implements ParticleThrower {
    @Inject(method = "Lcom/streetart/item/PressureWasherItem;onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD")
    )
    private void streetArt$evilSelfMixinForClientClassAccess(Level level, LivingEntity livingEntity, ItemStack itemStack,
                                                             int ticksRemaining, CallbackInfo ci) {
        if (level.isClientSide() && livingEntity instanceof Player player) {
            this.throwParticles(level, player, itemStack);
        }
    }

    @Override
    public Vector3f firstPersonPlane() {
        return new Vector3f(0.25f, -0.05f, 2);
    }

    @Override
    public Vec2 thirdPersonOffset() {
        return new Vec2(0.35f, 1.6f);
    }

    @Override
    public ParticleOptions getParticleThrown(Player player, ItemStack itemStack) {
        return ParticleTypes.SPLASH;
    }
}
