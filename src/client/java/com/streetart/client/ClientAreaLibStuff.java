package com.streetart.client;

import com.streetart.arealib.AreaLibLib;
import com.streetart.item.AreaModifierItem;
import dev.doublekekse.area_lib.component.SampledAreaComponentType;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;

public class ClientAreaLibStuff {
    public static void init() {
        LevelRenderEvents.BEFORE_GIZMOS.register((context) -> {
            final ClientLevel level = Minecraft.getInstance().level;

            if (level == null) {
                return;
            }

            final Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            if (!(player.getMainHandItem().getItem() instanceof final AreaModifierItem areaModifierItem)) {
                return;
            }

            final SampledAreaComponentType<Unit> areaType = AreaLibLib.getComponent(areaModifierItem.areaType);

            final AreaSavedData savedData = AreaClientData.getClientLevelData();
            if (savedData == null) {
                return;
            }

            final MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
            if (gameMode == null) {
                return;
            }
            if (gameMode.getPlayerMode().isSurvival()) {
                return;
            }

            final var poseStack = context.poseStack();
            if (poseStack == null) {
                return;
            }

            poseStack.pushPose();

            final var cPos = context.levelState().cameraRenderState.pos;
            poseStack.translate(-cPos.x, -cPos.y, -cPos.z);

            if (savedData != null) {
                final Identifier dimension = level.dimension().identifier();
                savedData.getAreas().forEach(area -> {
                    if (area.has(areaType)) {
                        area.render(context, poseStack, dimension);
                    }
                });
            }

            poseStack.popPose();
        });
    }
}
