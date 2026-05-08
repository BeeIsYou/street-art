package com.streetart.item;

import com.streetart.AllDataComponents;
import com.streetart.AllItems;
import com.streetart.StreetArt;
import com.streetart.component.TapeRecorderContents;
import com.streetart.tracks.RecordedTrack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;

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
                } else {
                    final RecordedTrack track = contents.getContained().get(AllDataComponents.TRACK_RECORDING);
                    if (track != null) {
                        if (track.running) {
                            int sec = track.progress / 20;
                            final int min = sec / 60;
                            sec %= 60;
                            final double ms = (track.getPoints().size() % 20) / 20d;
                            final DecimalFormat formatter = new DecimalFormat("#.00");
                            player.sendOverlayMessage(Component.literal(String.format("%d:%02d%s", min, sec, formatter.format(ms))));
                        } else {
                            track.progress = 0;
                            track.partialTick = 0;
                        }
                        if (player.isShiftKeyDown()) {
                            track.running = false;
                            track.progress = 0;
                            track.partialTick = 0;
                        } else {
                            track.running = !track.running;
                        }
                    }
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    public static @Nullable RecordedTrack findTrackInHand(final Player player) {
        ItemStack item = player.getMainHandItem();
        TapeRecorderContents contents = item.get(AllDataComponents.TAPE_RECORDER_CONTENTS);
        if (contents != null) {
            final RecordedTrack track = contents.getContained().get(AllDataComponents.TRACK_RECORDING);
            if (track != null) {
                return track;
            }
        }

        item = player.getOffhandItem();
        contents = item.get(AllDataComponents.TAPE_RECORDER_CONTENTS);
        if (contents != null) {
            final RecordedTrack track = contents.getContained().get(AllDataComponents.TRACK_RECORDING);
            if (track != null) {
                return track;
            }
        }

        return null;
    }

    public static void tickInventoryProgress(final Player player) {
        for (final ItemStack itemStack : player.getInventory()) {
            final TapeRecorderContents contents = itemStack.get(AllDataComponents.TAPE_RECORDER_CONTENTS);
            if (contents != null) {
                final RecordedTrack track = contents.getContained().get(AllDataComponents.TRACK_RECORDING);
                if (track != null) {
                    if (track.running) {
                        track.progress++;
                    }
                }
            }
        }
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
