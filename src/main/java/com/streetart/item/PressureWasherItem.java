package com.streetart.item;

import com.streetart.component.ColorComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PressureWasherItem extends Item implements SprayPaintInteractor {
    public PressureWasherItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (this.hasColor(player, player.getItemInHand(hand))) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean canDestroyBlock(final ItemStack itemStack, final BlockState state, final Level level, final BlockPos pos, final LivingEntity user) {
        return false;
    }

    @Override
    public int getUseDuration(final ItemStack itemStack, final LivingEntity user) {
        return 1200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(final ItemStack itemStack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public void onUseTick(final Level level, final LivingEntity livingEntity, final ItemStack itemStack, final int ticksRemaining) {
        // evil self mixin here
    }

    @Override
    public Vec3 getLookVector(final Player player, final Vec2 originalRot, final Vec3 forward, final ItemStack itemStack, final float pt, final boolean rightClick) {
        final Vec3 up = player.calculateViewVector(originalRot.x + 90, originalRot.y);
        final Vec3 left = forward.cross(up);

        double dx = player.getRandom().nextDouble() * 0.25  - 0.125;
        double dy = player.getRandom().nextDouble() * 0.05 - 0.025;

        if (!rightClick) {
            final double temp = dx;
            dx = dy;
            dy = temp;
        }
        return forward
                .add(left.scale(dx))
                .add(up.scale(dy))
                .normalize();
    }

    @Override
    public int iterationsPerTick(final Player player, final ItemStack itemStack) {
        return 64;
    }

    @Override
    public boolean hasColor(final Player player, final ItemStack itemStack) {
        return true;
    }

    @Override
    public ColorComponent getColor(final Player player, final ItemStack itemStack) {
        return ColorComponent.CLEAR;
    }

    @Override
    public ParticleOptions getParticleAtPoint(final Player player, final ItemStack itemStack) {
        return ParticleTypes.FALLING_WATER;
    }

    @Override
    public InteractionResult interactLivingEntity(final ItemStack itemStack, final Player player, final LivingEntity target, final InteractionHand type) {
        if (!player.level().isClientSide()) {
            if (target instanceof Blaze) {
                target.hurtServer((ServerLevel) player.level(), target.damageSources().drown(), 2);
            }
        }

        return super.interactLivingEntity(itemStack, player, target, type);
    }
}
