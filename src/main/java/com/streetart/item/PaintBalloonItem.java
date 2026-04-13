package com.streetart.item;

import com.streetart.ArtUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;

public class PaintBalloonItem extends Item {
    public PaintBalloonItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        if (context.getLevel() instanceof final ServerLevel serverLevel) {
            DyedItemColor dyedColor = context.getItemInHand().get(DataComponents.DYED_COLOR);
            int color = 0;
            if (dyedColor != null) {
                color = dyedColor.rgb();
            }
            ArtUtil.paintExplosion(
                    serverLevel,
                    context.getClickLocation().add(context.getClickedFace().getUnitVec3().scale(0.5)),
                    color
            );
        }

        return InteractionResult.SUCCESS;
    }
}
