package com.streetart.tracks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class TrackRecording {
    public static final int MAX_POINTS = 20*60*2;

    public static final Codec<TrackRecording> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Point.CODEC.sizeLimitedListOf(MAX_POINTS).fieldOf("points").forGetter(r -> r.currentRecording)
    ).apply(instance, TrackRecording::new));

    public static final StreamCodec<ByteBuf, TrackRecording> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, Point.STREAM_CODEC, MAX_POINTS),
            r -> r.currentRecording,
            TrackRecording::new
    );

    private final List<Point> currentRecording;
    private boolean nextSignificant = false;

    public TrackRecording(final List<Point> recording) {
        this.currentRecording = recording;
    }

    public List<Point> getPoints() {
        return this.currentRecording;
    }

    public void tickRecording(final Entity recorder) {
        if (this.currentRecording.size() < MAX_POINTS) {
            this.currentRecording.add(new Point(
                    recorder.position().x,
                    recorder.position().y,
                    recorder.position().z,
                    this.nextSignificant
            ));
            this.nextSignificant = false;
        }
    }

    public void markSignificant() {
        this.nextSignificant = true;
    }

    public boolean needsToStop() {
        return this.currentRecording.size() == MAX_POINTS;
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
