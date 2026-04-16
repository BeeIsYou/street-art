package com.streetart.client.mixin.rollerblades;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
//    @Unique
//    private
    public LocalPlayerMixin(final Level level, final GameProfile gameProfile) {
        super(level, gameProfile);
    }
}
