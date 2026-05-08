package com.streetart.client.mixin;

import com.streetart.AllDataComponents;
import com.streetart.client.PaintPlacerUtil;
import com.streetart.component.paint_placer.PaintPlacerComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "onUseTick",
            at = @At("HEAD")
    )
    private void streetArt$throwParticles(final Level level, final LivingEntity livingEntity, final ItemStack itemStack,
                                          final int ticksRemaining, final CallbackInfo ci) {
        if (level.isClientSide() && livingEntity instanceof final Player player) {
            final PaintPlacerComponent placer = itemStack.get(AllDataComponents.PAINT_PLACER);
            if (placer != null) {
                PaintPlacerUtil.throwParticles(level, player, placer);
            }
        }
    }
}
