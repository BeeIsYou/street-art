package com.streetart;

import com.streetart.managers.GraffitiGlobalManager;
import com.streetart.managers.GraffitiLevelManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.awt.*;

public class SprayCanItem extends Item {
    public SprayCanItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level level = context.getLevel();

        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        } else {
            final GraffitiLevelManager manager = GraffitiGlobalManager.getGraffitiLevelManager((ServerLevel) level);
            manager.createAndPopulateGraffiti(context.getClickedPos(), context.getClickLocation(), context.getClickedFace(), Color.CYAN.getRGB());
        }

        return InteractionResult.PASS;
    }
}
