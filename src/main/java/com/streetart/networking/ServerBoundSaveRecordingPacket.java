package com.streetart.networking;

import com.streetart.AllDataComponents;
import com.streetart.AllItems;
import com.streetart.StreetArt;
import com.streetart.component.TapeRecorderContents;
import com.streetart.tracks.TrackRecording;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record ServerBoundSaveRecordingPacket(TrackRecording recording) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerBoundSaveRecordingPacket> TYPE  = new Type<>(StreetArt.id("save_recording"));
    public static final StreamCodec<ByteBuf, ServerBoundSaveRecordingPacket> CODEC = StreamCodec.composite(
            TrackRecording.STREAM_CODEC,
            ServerBoundSaveRecordingPacket::recording,
            ServerBoundSaveRecordingPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerBoundSaveRecordingPacket packet, final ServerPlayNetworking.Context context) {
        for (final ItemStack itemStack : context.player().getInventory()) {
            final TapeRecorderContents contents = itemStack.get(AllDataComponents.TAPE_RECORDER_CONTENTS);
            if (contents != null) {
                final ItemStack contained = contents.getContained();
                if (contained.is(AllItems.BLANK_TRACK)) {
                    final ItemStack track = contained.transmuteCopy(AllItems.TRACK);
                    track.set(AllDataComponents.TRACK_RECORDING,
                            new TrackRecording(context.player().getPlainTextName(), packet.recording.getPoints())
                    );
                    itemStack.set(AllDataComponents.TAPE_RECORDER_CONTENTS, new TapeRecorderContents(track));
                    context.player().sendOverlayMessage(Component.translatable("street_art.tape_recorder.message.success"));
                    return;
                }
            }
        }
        context.player().sendOverlayMessage(Component.translatable("street_art.tape_recorder.message.failure"));
    }
}
