package com.streetart.item;

import com.streetart.AllDataComponents;
import com.streetart.component.ColorComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class SprayCanItem extends Item implements SprayPaintInteractor {
    public SprayCanItem(final Properties properties) {
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
    public InteractionResult interactLivingEntity(final ItemStack itemStack, final Player player, final LivingEntity target, final InteractionHand type) {
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(final Level level, final LivingEntity livingEntity, final ItemStack itemStack, final int ticksRemaining) {
        // evil self mixin here
    }

    @Override
    public Vec3 getLookVector(final Player player, final Vec2 originalRot, final Vec3 forward, final ItemStack itemStack, final float pt, final boolean rightClick) {
        final Vec3 up = player.calculateViewVector(originalRot.x + 90, originalRot.y);
        final Vec3 left = forward.cross(up);

        double dx = player.getRandom().nextGaussian() * 0.04;
        double dy = player.getRandom().nextGaussian() * 0.04;

        if (!rightClick) {
            dx *= 0.2;
            dy *= 0.2;
        }

        return forward
                .add(left.scale(dx))
                .add(up.scale(dy))
                .normalize();
    }

    @Override
    public int iterationsPerTick(final Player player, final ItemStack itemStack) {
        return 32;
    }

    @Override
    public boolean hasColor(final Player player, final ItemStack itemStack) {
        return itemStack.has(AllDataComponents.COLOR);
    }

    @Override
    public int getColor(final Player player, final ItemStack itemStack) {
        return ColorComponent.getOrDefaultOpaque(itemStack, ColorComponent.BLACK.argb);
    }

    @Override
    public ParticleOptions getParticleAtPoint(final Player player, final ItemStack itemStack) {
        final int color = this.getColor(player, itemStack);
        return new DustParticleOptions(color, 1);
    }
}
