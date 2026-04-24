package com.streetart.item;

import com.streetart.AllDataComponents;
import com.streetart.AllItems;
import com.streetart.StreetArt;
import com.streetart.component.TapeRecorderContents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TapeRecorderItem extends Item {
    public TapeRecorderItem(final Properties properties) {
        super(properties);
    }

    public static boolean hasRecorderWithBlankTrack(final Player player) {
        for (final ItemStack itemStack : player.getInventory()) {
            final TapeRecorderContents contents = itemStack.get(AllDataComponents.TAPE_RECORDER_CONTENTS);
            if (contents != null) {
                final ItemStack contained = contents.getContained();
                if (contained.is(AllItems.BLANK_TRACK)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (level.isClientSide()) {
            final TapeRecorderContents contents = player.getItemInHand(hand).get(AllDataComponents.TAPE_RECORDER_CONTENTS);
            if (contents != null) {
                if (contents.getContained().is(AllItems.BLANK_TRACK)) {
                    StreetArt.recordingManager.itemUseEmptyTrack(player);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canDestroyBlock(final ItemStack itemStack, final BlockState state, final Level level, final BlockPos pos, final LivingEntity user) {
        return super.canDestroyBlock(itemStack, state, level, pos, user);
    }

    @Override
    public boolean overrideOtherStackedOnMe(final ItemStack self, final ItemStack other, final Slot slot, final ClickAction clickAction, final Player player, final SlotAccess carriedItem) {
        if (clickAction != ClickAction.SECONDARY) {
            return false;
        }

        if (!slot.allowModification(player)) {
            return false;
        }

        final TapeRecorderContents contents = self.get(AllDataComponents.TAPE_RECORDER_CONTENTS);

        if (contents == null || contents.getContained().isEmpty()) {
            if (TapeRecorderContents.accepts(other)) {
                // putting tape into recorder
                self.set(AllDataComponents.TAPE_RECORDER_CONTENTS, new TapeRecorderContents(other.copyWithCount(1)));
                other.shrink(1);
                return true;
            }
        } else {
            if (other.isEmpty()) {
                // taking tape out of recorder
                carriedItem.set(contents.getContained());
                self.remove(AllDataComponents.TAPE_RECORDER_CONTENTS);
                return true;
            } else if (TapeRecorderContents.accepts(other) && other.getCount() == 1) {
                // swapping tapes
                final TapeRecorderContents newContents = new TapeRecorderContents(other);
                carriedItem.set(contents.getContained());
                self.set(AllDataComponents.TAPE_RECORDER_CONTENTS, newContents);
                return true;
            }
        }

        return false;
    }
}
