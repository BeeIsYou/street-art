package com.streetart.client.mixin;

import com.streetart.client.ParticleThrower;
import com.streetart.item.SprayCanItem;
import com.streetart.item.SprayPaintInteractor;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
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

@Mixin(SprayCanItem.class)
public abstract class SprayCanItemMixin implements SprayPaintInteractor, ParticleThrower {
    @Inject(method = "Lcom/streetart/item/SprayCanItem;onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V",
        at = @At("HEAD")
    )
    private void streetArt$evilSelfMixinForClientClassAccess(final Level level, final LivingEntity livingEntity, final ItemStack itemStack,
                                                             final int ticksRemaining, final CallbackInfo ci) {
        if (level.isClientSide() && livingEntity instanceof final Player player) {
            this.throwParticles(level, player, itemStack);
        }
    }

    @Override
    public Vector3f firstPersonPlane() {
        return new Vector3f(0.525f, -0.1f, 1);
    }

    @Override
    public Vec2 thirdPersonOffset() {
        return new Vec2(0.35f, 0.8f);
    }

    @Override
    public ParticleOptions getParticleThrown(final Player player, final ItemStack itemStack) {
        final int color = this.getColor(player, itemStack);
        return new DustParticleOptions(color, 1);
    }
}
