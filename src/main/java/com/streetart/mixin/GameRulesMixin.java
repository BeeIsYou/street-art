package com.streetart.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.streetart.networking.ClientBoundGameRuleSync;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRules.class)
public class GameRulesMixin {
    @Inject(method = "Lnet/minecraft/world/level/gamerules/GameRules;set(Lnet/minecraft/world/level/gamerules/GameRule;Ljava/lang/Object;Lnet/minecraft/server/MinecraftServer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;onGameRuleChanged(Lnet/minecraft/world/level/gamerules/GameRule;Ljava/lang/Object;)V")
    )
    private void streetArt$NotifyGameRule(final CallbackInfo ci,
                                          @Local(argsOnly = true) final MinecraftServer server,
                                          @Local(argsOnly = true) final GameRule<?> gameRule) {
        if (ClientBoundGameRuleSync.shouldUpdate(gameRule)) {
            final ClientBoundGameRuleSync packet = ClientBoundGameRuleSync.fromServer(server);
            for (final ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, packet);
            }
        }
    }
}
