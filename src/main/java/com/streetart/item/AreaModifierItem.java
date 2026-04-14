package com.streetart.item;

import com.streetart.AllDataComponents;
import com.streetart.StreetArt;
import com.streetart.arealib.AreaLib;
import com.streetart.component.AreaSelectComponent;
import net.minecraft.commands.Commands;
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

import java.util.function.Function;

public class AreaModifierItem extends Item {
    public final AreaLib.Type areaType;
    public AreaModifierItem(final Properties properties, final AreaLib.Type type) {
        super(properties);
        this.areaType = type;
    }

    public static Function<Properties, AreaModifierItem> forType(final AreaLib.Type type) {
        return p -> new AreaModifierItem(p, type);
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
        if (user instanceof final ServerPlayer player) {
            if (!Commands.LEVEL_ADMINS.check(player.permissions())) {
                return false;
            }

            StreetArt.AREA_LIB.removeRegion(player.level(), this.areaType, pos);
        }
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
            if (!Commands.LEVEL_ADMINS.check(player.permissions())) {
                return false;
            }

            final HitResult hit = entity.pick(player.blockInteractionRange(), 0, false);
            if (hit instanceof final BlockHitResult hitResult) {
                StreetArt.AREA_LIB.createRegion(player.level(), this.areaType, areaSelect.start(), hitResult.getBlockPos());
            }
        }
        return false;
    }
}
