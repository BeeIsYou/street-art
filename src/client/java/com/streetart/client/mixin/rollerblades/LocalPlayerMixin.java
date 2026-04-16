package com.streetart.client.mixin.rollerblades;

import com.mojang.authlib.GameProfile;
import com.streetart.schmoovement.RollerBlades;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
//    @Unique
//    private
    public LocalPlayerMixin(final Level level, final GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @ModifyVariable(method = "applyInput", at = @At("STORE"))
    private Vec2 streetArt$modifyRollerBladeInput(final Vec2 original) {
        if (RollerBlades.canRoll(this)) {
            return RollerBlades.handleInput(original, this);
        }
        return original;
    }
}
