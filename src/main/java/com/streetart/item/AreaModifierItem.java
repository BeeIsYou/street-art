package com.streetart.item;

import com.streetart.AllDataComponents;
import com.streetart.StreetArt;
import com.streetart.arealib.AreaLiblessLib;
import com.streetart.component.AreaSelectComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AreaModifierItem extends Item {
    public final AreaLiblessLib.AreaType areaType;
    public AreaModifierItem(final Properties properties, final AreaLiblessLib.AreaType areaType) {
        super(properties);
        this.areaType = areaType;
    }


    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        final HitResult hit = player.pick(player.blockInteractionRange(), 0, false);
        if (hit instanceof final BlockHitResult hitResult) {
            player.getItemInHand(hand).set(AllDataComponents.AREA_SELECT, new AreaSelectComponent(hitResult.getBlockPos()));
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
    public boolean releaseUsing(final ItemStack itemStack, final Level level, final LivingEntity entity, final int remainingTime) {
        final AreaSelectComponent areaSelect = itemStack.get(AllDataComponents.AREA_SELECT);
        if (entity instanceof final ServerPlayer player && areaSelect != null) {
            final HitResult hit = entity.pick(player.blockInteractionRange(), 0, false);
            if (hit instanceof final BlockHitResult hitResult) {
                StreetArt.AREA_LIB.createRegion(
                        level,
                        player.level().getServer(),
                        this.areaType,
                        areaSelect.start(),
                        hitResult.getBlockPos()
                );
            }
        }
        return super.releaseUsing(itemStack, level, entity, remainingTime);
    }
}
