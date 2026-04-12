package com.streetart;

import com.streetart.components.Focused;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class SprayCanItem extends Item implements SprayPaintInteractor {
    public SprayCanItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            boolean isFocused = Focused.toggle(itemStack);
            if (player.level().isClientSide()) {
                player.sendOverlayMessage(Focused.getKey(itemStack));
            }
            return InteractionResult.SUCCESS;
        }
        if (this.hasColor(player, player.getItemInHand(hand))) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return 1200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand type) {
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        return InteractionResult.CONSUME;
    }

    @Override
    public Vec3 getLookVector(Player player, Vec2 originalRot, Vec3 forward, ItemStack itemStack, float pt) {
        Vec3 left = player.calculateViewVector(originalRot.x + 90, originalRot.y);
        Vec3 up = forward.cross(left);

        double dx = player.getRandom().nextGaussian() * 0.04;
        double dy = player.getRandom().nextGaussian() * 0.04;

        if (Focused.is(itemStack)) {
            dx *= 0.2;
            dy *= 0.2;
        }

        return forward
                .add(left.scale(dx))
                .add(up.scale(dy))
                .normalize();
    }

    @Override
    public int iterationsPerTick(Player player, ItemStack itemStack) {
        return 32;
    }

    @Override
    public boolean hasColor(Player player, ItemStack itemStack) {
        return itemStack.has(DataComponents.DYED_COLOR);
    }

    @Override
    public int getColor(Player player, ItemStack itemStack) {
        return itemStack.get(DataComponents.DYED_COLOR).rgb();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        Focused focused = itemStack.get(AllDataComponents.FOCUSED);
        if (focused != null) {
            focused.addToTooltip(context, builder, tooltipFlag, itemStack);
        }
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}
