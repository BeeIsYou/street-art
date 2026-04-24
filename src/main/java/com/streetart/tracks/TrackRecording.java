package com.streetart.tracks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TrackRecording implements TooltipProvider {
    public static final int MAX_POINTS = 20*60*2;

    public static final Codec<TrackRecording> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("author").forGetter(r -> r.author),
            Point.CODEC.sizeLimitedListOf(MAX_POINTS).fieldOf("points").forGetter(r -> r.recordedTrack)
    ).apply(instance, TrackRecording::new));

    public static final StreamCodec<ByteBuf, TrackRecording> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            r -> r.author,
            ByteBufCodecs.collection(ArrayList::new, Point.STREAM_CODEC, MAX_POINTS),
            r -> r.recordedTrack,
            TrackRecording::new
    );

    private final String author;
    private final List<Point> recordedTrack;
    private boolean nextSignificant = false;

    public TrackRecording(final String author, final List<Point> recording) {
        this.author = author;
        this.recordedTrack = recording;
    }

    public List<Point> getPoints() {
        return this.recordedTrack;
    }

    public void tickRecording(final Entity recorder) {
        if (this.recordedTrack.size() < MAX_POINTS) {
            this.recordedTrack.add(new Point(
                    recorder.position().x,
                    recorder.position().y + 0.1,
                    recorder.position().z,
                    this.nextSignificant
            ));
            this.nextSignificant = false;
        }
    }

    public float getDuration() {
        return this.recordedTrack.size() / 20f;
    }

    public void markSignificant() {
        this.nextSignificant = true;
    }

    public boolean needsToStop() {
        return this.recordedTrack.size() == MAX_POINTS;
    }

    @Override
    public void addToTooltip(final Item.TooltipContext context, final Consumer<Component> consumer, final TooltipFlag flag, final DataComponentGetter components) {
        if (!StringUtil.isBlank(this.author)) {
            consumer.accept(Component.translatable("street_art.track.author", this.author).withStyle(ChatFormatting.RED));
        }
        final int seconds = Mth.ceil(this.getDuration());
        final int min = seconds / 60;
        final int sec = seconds - min * 60;
        final MutableComponent component;
        if (min == 0) {
            component = Component.translatable("street_art.track.duration.seconds", sec);
        } else {
            component = Component.translatable("street_art.track.duration.minutes_seconds", min, String.format("%02d", sec));
        }
        consumer.accept(component.withStyle(ChatFormatting.GOLD));

        if (!this.recordedTrack.isEmpty()) {
            final BlockPos origin = BlockPos.containing(
                    this.recordedTrack.getFirst().x,
                    this.recordedTrack.getFirst().y,
                    this.recordedTrack.getFirst().z
            );
            consumer.accept(Component.translatable("street_art.track.start_position", origin.getX(), origin.getY(), origin.getZ()).withStyle(ChatFormatting.GRAY));
        }

    }

    public record Point(double x, double y, double z, boolean significant) {
        public static final Codec<Point> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter(Point::x),
                Codec.DOUBLE.fieldOf("y").forGetter(Point::y),
                Codec.DOUBLE.fieldOf("z").forGetter(Point::z),
                Codec.BOOL.fieldOf("significant").forGetter(Point::significant)
        ).apply(instance, Point::new));

        public static final StreamCodec<ByteBuf, Point> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.DOUBLE,
                Point::x,
                ByteBufCodecs.DOUBLE,
                Point::y,
                ByteBufCodecs.DOUBLE,
                Point::z,
                ByteBufCodecs.BOOL,
                Point::significant,
                Point::new
        );
    }
}
