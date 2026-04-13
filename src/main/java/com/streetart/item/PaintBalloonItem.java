package com.streetart.item;

import com.streetart.ArtUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class PaintBalloonItem extends Item {
    public PaintBalloonItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        if (context.getLevel() instanceof final ServerLevel serverLevel) {
            ArtUtil.paintExplosion(
                    serverLevel,
                    context.getClickLocation().add(context.getClickedFace().getUnitVec3().scale(0.5)),
                    DyeColor.MAGENTA.getTextureDiffuseColor()
            );
        }

        return InteractionResult.SUCCESS;
    }
}
