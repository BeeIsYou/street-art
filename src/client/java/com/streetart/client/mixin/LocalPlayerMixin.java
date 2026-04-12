package com.streetart.client.mixin;

import com.mojang.authlib.GameProfile;
import com.streetart.client.manager.SpraySessionManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    public LocalPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public void turn(double xo, double yo) {
        super.turn(xo, yo);
        SpraySessionManager.playerTurned(this);
    }
}
