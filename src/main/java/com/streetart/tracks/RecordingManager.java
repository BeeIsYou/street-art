package com.streetart.tracks;

import com.streetart.AllDataComponents;
import com.streetart.component.TapeRecorderContents;
import com.streetart.item.TapeRecorderItem;
import com.streetart.networking.ServerBoundSaveRecordingPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecordingManager {
    private List<RecordedTrack.Point> points = null;

    public void itemUseEmptyTrack(final Player player) {
        if (this.points == null) {
            this.start(player);
        } else {
            this.stop();
        }
    }

    public List<RecordedTrack.Point> getPoints() {
        return this.points;
    }

    public Collection<RecordedTrack> findInventoryRecordings(final Player player) {
        final List<RecordedTrack> recordings = new ArrayList<>();
        for (final ItemStack itemStack : player.getInventory()) {
            final TapeRecorderContents contents = itemStack.get(AllDataComponents.TAPE_RECORDER_CONTENTS);
            if (contents != null) {
                final ItemStack contained = contents.getContained();
                final RecordedTrack recording = contained.get(AllDataComponents.TRACK_RECORDING);
                if (recording != null) {
                    recordings.add(recording);
                }
            }
        }
        return recordings;
    }

    public void start(final Player player) {
        if (TapeRecorderItem.hasRecorderWithBlankTrack(player)) {
            this.points = new ArrayList<>(RecordedTrack.MAX_POINTS);
            player.sendOverlayMessage(Component.translatable("street_art.tape_recorder.message.start"));
        }
    }

    public void tick(final Player player, final Level level) {
        if (this.points != null) {
            if (player == null || level == null || !TapeRecorderItem.hasRecorderWithBlankTrack(player)) {
                this.cancel(player);
            } else {
                this.tickRecording(player);
                if (this.needsToStop()) {
                    this.stop();
                }
            }
        }
    }

    public void tickRecording(final Entity recorder) {
        if (this.points.size() < RecordedTrack.MAX_POINTS) {
            this.points.add(new RecordedTrack.Point(
                    recorder.position().x,
                    recorder.position().y + 0.1,
                    recorder.position().z
            ));
        }
    }

    public boolean needsToStop() {
        return this.points.size() == RecordedTrack.MAX_POINTS;
    }

    public void cancel(final Player player) {
        this.points = null;
        if (player != null) {
            player.sendOverlayMessage(Component.translatable("street_art.tape_recorder.message.cancel"));
        }
    }

    public void stop() {
        ClientPlayNetworking.send(new ServerBoundSaveRecordingPacket(this.points));
        this.points = null;
        // message sent when server gives item with recording
    }
}
