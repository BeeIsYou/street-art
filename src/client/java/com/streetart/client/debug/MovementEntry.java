package com.streetart.client.debug;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class MovementEntry implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            displayer.addToGroup(AllDebugEntries.SCHMOOVEMENT,
                List.of(
                    ChatFormatting.UNDERLINE + "Schmoovement",
                    String.format("%.2f %.2f %.2f",
                        player.getDeltaMovement().x,
                        player.getDeltaMovement().y,
                        player.getDeltaMovement().z
                    ),
                    String.format("%.2f", player.getDeltaMovement().horizontalDistance()),
                    String.format("%.2f %.2f", player.xxa, player.zza)
                )
            );
        }
    }
}
