package com.streetart.mixin;

import com.streetart.AllDataComponents;
import com.streetart.component.paint_placer.PaintPlacerComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void streetArt$componentUseOverride(final Level level, final Player player, final InteractionHand hand,
                                                final CallbackInfoReturnable<InteractionResult> cir) {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.has(AllDataComponents.PAINT_PLACER)) {
            cir.setReturnValue(ItemUtils.startUsingInstantly(level, player, hand));
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void streetArt$componentUseDurationOverride(final ItemStack itemStack, final LivingEntity user,
                                                        final CallbackInfoReturnable<Integer> cir) {
        if (itemStack.has(AllDataComponents.PAINT_PLACER)) {
            cir.setReturnValue(1200);
        }
    }

    @Inject(method = "canDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void streetArt$destroyBlockOverride(final ItemStack itemStack, final BlockState state, final Level level, final BlockPos pos, final LivingEntity user,
                                                final CallbackInfoReturnable<Boolean> cir) {
        final PaintPlacerComponent paintPlacer = itemStack.get(AllDataComponents.PAINT_PLACER);
        if (paintPlacer != null && paintPlacer.leftClick().isPresent()) {
            cir.setReturnValue(false);
        }
    }
}
