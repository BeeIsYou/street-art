package com.streetart.client.debug;

import com.streetart.mixinterface.IHasRollerbladeController;
import com.streetart.schmoovement.RollerbladeController;
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
            RollerbladeController controller = ((IHasRollerbladeController)player).getController();
            displayer.addToGroup(AllDebugEntries.SCHMOOVEMENT,
                List.of(
                    ChatFormatting.UNDERLINE + "Schmoovement",
                    String.format("Vel %.2f %.2f %.2f",
                        player.getDeltaMovement().x,
                        player.getDeltaMovement().y,
                        player.getDeltaMovement().z
                    ),
                    String.format("Speed %.2f", player.getDeltaMovement().horizontalDistance()),
                    String.format("Input %.2f %.2f", player.xxa, player.zza),
                    String.format("Org Accel: %.5f %.5f", controller.debugOriginalAccel.x, controller.debugOriginalAccel.y),
                    String.format("Cap Accel: %.5f %.5f", controller.debugCappedAccel.x, controller.debugCappedAccel.y),
                    String.format("Fin Accel: %.5f %.5f", controller.debugFinalAccel.x, controller.debugFinalAccel.y),
                    String.format("Cap ratio: %.2f", controller.debugCapRatio),
                    String.format("Crouch: %d", controller.debugCrouchingTicks),
                    String.format("Stride: %.2f", controller.debugStride)
                )
            );
        }
    }
}
