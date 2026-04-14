package com.streetart.networking;

import com.streetart.AllGameRules;
import com.streetart.StreetArt;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRule;

public record ClientBoundGameRuleSync(
                boolean adventurePainting
        ) implements CustomPacketPayload {
    public static ClientBoundGameRuleSync CLIENT_CURRENT = new ClientBoundGameRuleSync(false);

    public static final CustomPacketPayload.Type<ClientBoundGameRuleSync> TYPE = new Type<>(StreetArt.id("game_rule_sync"));
    public static final StreamCodec<ByteBuf, ClientBoundGameRuleSync> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientBoundGameRuleSync::adventurePainting,
            ClientBoundGameRuleSync::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static boolean shouldUpdate(final GameRule<?> rule) {
        return rule == AllGameRules.ADVENTURE_PAINTING;
    }

    public static ClientBoundGameRuleSync fromServer(final MinecraftServer server) {
        return new ClientBoundGameRuleSync(
                server.getGameRules().get(AllGameRules.ADVENTURE_PAINTING)
        );
    }

    public static ClientBoundGameRuleSync get(final Level level) {
        if (level instanceof final ServerLevel serverLevel) {
            return fromServer(serverLevel.getServer());
        }
        return CLIENT_CURRENT;
    }

    public static void onJoin(final ServerPlayer player) {
        ServerPlayNetworking.send(player, fromServer(player.level().getServer()));
    }
}
