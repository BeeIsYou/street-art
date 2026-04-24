package com.streetart.networking;

import com.streetart.AllDataComponents;
import com.streetart.AllItems;
import com.streetart.StreetArt;
import com.streetart.component.TapeRecorderContents;
import com.streetart.tracks.RecordedTrack;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ServerBoundSaveRecordingPacket(List<RecordedTrack.Point> points) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerBoundSaveRecordingPacket> TYPE  = new Type<>(StreetArt.id("save_recording"));
    public static final StreamCodec<ByteBuf, ServerBoundSaveRecordingPacket> CODEC = StreamCodec.composite(
            RecordedTrack.Point.STREAM_CODEC.apply(ByteBufCodecs.list(RecordedTrack.MAX_POINTS)),
            ServerBoundSaveRecordingPacket::points,
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
                    final RandomSource random = context.player().getRandom();
                    final int dyeIndex1 = random.nextInt(DyeColor.values().length);
                    int dyeIndex2 = random.nextInt(DyeColor.values().length - 1);
                    if (dyeIndex2 >= dyeIndex1) {
                        dyeIndex2++;
                    }
                    track.set(AllDataComponents.TRACK_RECORDING,
                            new RecordedTrack(
                                    context.player().getPlainTextName(),
                                    packet.points,
                                    DyeColor.values()[dyeIndex1],
                                    DyeColor.values()[dyeIndex2]
                            )
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
