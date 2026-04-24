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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RecordedTrack implements TooltipProvider {
    public static final int MAX_POINTS = 20*60*2;

    public static final Codec<RecordedTrack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("author").forGetter(r -> r.author),
            Point.CODEC.sizeLimitedListOf(MAX_POINTS).fieldOf("points").forGetter(r -> r.track),
            DyeColor.CODEC.fieldOf("color_a").forGetter(r -> r.colorA),
            DyeColor.CODEC.fieldOf("color_b").forGetter(r -> r.colorB)
    ).apply(instance, RecordedTrack::new));

    public static final StreamCodec<ByteBuf, RecordedTrack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            r -> r.author,
            ByteBufCodecs.collection(ArrayList::new, Point.STREAM_CODEC, MAX_POINTS),
            r -> r.track,
            DyeColor.STREAM_CODEC,
            r -> r.colorA,
            DyeColor.STREAM_CODEC,
            r -> r.colorB,
            RecordedTrack::new
    );

    private final String author;
    private final List<Point> track;
    public final DyeColor colorA;
    public final DyeColor colorB;

    public RecordedTrack(final String author, final List<Point> recording, DyeColor colorA, DyeColor colorB) {
        this.author = author;
        this.track = recording;
        this.colorA = colorA;
        this.colorB = colorB;
    }

    public RecordedTrack redye(DyeColor colorA, DyeColor colorB) {
        return new RecordedTrack(
                this.author,
                this.track,
                colorA,
                colorB
        );
    }

    public List<Point> getPoints() {
        return this.track;
    }


    public float getDuration() {
        return this.track.size() / 20f;
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

        if (!this.track.isEmpty()) {
            final BlockPos origin = BlockPos.containing(
                    this.track.getFirst().x,
                    this.track.getFirst().y,
                    this.track.getFirst().z
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
