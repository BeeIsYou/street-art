package com.streetart.client.mixin;

import com.streetart.SprayCanItem;
import com.streetart.SprayPaintInteractor;
import com.streetart.client.ArtUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SprayCanItem.class)
public abstract class SprayCanItemMixin implements SprayPaintInteractor {
    @Inject(method = "Lcom/streetart/SprayCanItem;onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V",
        at = @At("HEAD")
    )
    private void streetArt$evilSelfMixinForClientClassAccess(Level level, LivingEntity livingEntity, ItemStack itemStack,
                                                           int ticksRemaining, CallbackInfo ci) {
        if (level.isClientSide() && livingEntity instanceof Player player) {
            int color = this.getColor(player, itemStack);
            Vec3 origin = ArtUtil.getParticleOrigin(
                    player,
                    Minecraft.getInstance().options,
                    Minecraft.getInstance().gameRenderer.getMainCamera()
            );
            Vec3 look = ArtUtil.getParticleDirection(player);

            level.addParticle(new DustParticleOptions(color, 1),
                    origin.x, origin.y, origin.z, look.x, look.y, look.z
            );
        }
    }
}
