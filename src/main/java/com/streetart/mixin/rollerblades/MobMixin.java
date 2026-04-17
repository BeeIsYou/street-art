package com.streetart.mixin.rollerblades;

import com.streetart.AllItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Shadow
    abstract void setItemSlotAndDropWhenKilled(EquipmentSlot slot, ItemStack itemStack);

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"))
    private void streetArt$spawnWithDrip(final RandomSource random, final DifficultyInstance difficulty, final CallbackInfo ci) {
        if (random.nextFloat() < 0.02f * difficulty.getSpecialMultiplier()) {
            final Item rollerBlade = AllItems.ROLLERBLADES.get(random.nextInt(AllItems.ROLLERBLADES.size()));
            this.setItemSlotAndDropWhenKilled(EquipmentSlot.FEET, new ItemStack(rollerBlade));
        }
    }
}
