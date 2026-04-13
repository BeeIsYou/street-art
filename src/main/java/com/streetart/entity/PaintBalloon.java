package com.streetart.entity;

import com.streetart.AllEntityTypes;
import com.streetart.AllItems;
import com.streetart.SplashUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PaintBalloon extends ThrowableItemProjectile {
    public PaintBalloon(final EntityType<PaintBalloon> type, final Level level) {
        super(type, level);
    }

    public PaintBalloon(final Level level, final LivingEntity mob, final ItemStack itemStack) {
        super(AllEntityTypes.PAINT_BALLOON, mob, level, itemStack);
    }

    public PaintBalloon(final Level level, final double x, final double y, final double z, final ItemStack itemStack) {
        super(AllEntityTypes.PAINT_BALLOON, x, y, z, level, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return AllItems.WATER_BALLOON;
    }

    @Override
    protected void onHitBlock(final BlockHitResult hitResult) {
        if (this.level() instanceof final ServerLevel serverLevel) {
            final Vec3 splashOrigin = hitResult.getLocation()
                    .add(hitResult.getDirection().getUnitVec3().scale(0.3))
                    .subtract(this.getDeltaMovement().scale(0.3));
            final DyedItemColor dyedColor = this.getItem().get(DataComponents.DYED_COLOR);
            int color = 0;
            if (dyedColor != null) {
                color = dyedColor.rgb();
            }
            SplashUtil.createPaintSplash(serverLevel, splashOrigin, 3, 1f, color);
        }
        super.onHitBlock(hitResult);
    }

    @Override
    public void handleEntityEvent(final byte id) {
        if (id == 3) {
            final DyedItemColor dyedColor = this.getItem().get(DataComponents.DYED_COLOR);
            final boolean water = dyedColor == null || dyedColor.rgb() == 0;

            final ParticleOptions particle;
            if (water) {
                particle = ParticleTypes.SPLASH;
            } else {
                final int color = dyedColor.rgb();
                particle = new DustParticleOptions(color, 1);
            }

            for (int i = 0; i < 32; i++) {
                this.level().addParticle(particle,
                    this.getX() + this.random.nextGaussian(),
                    this.getY() + this.random.nextGaussian(),
                    this.getZ() + this.random.nextGaussian(),
                    this.random.nextGaussian() * 0.1,
                    this.random.nextGaussian() * 0.1,
                    this.random.nextGaussian() * 0.1
                );
            }
            this.level().playLocalSound(this, SoundEvents.GENERIC_SPLASH, SoundSource.NEUTRAL, 1, 1);
        }
    }

    @Override
    protected void onHit(final HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}
