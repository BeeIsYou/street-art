package com.streetart.client.rendering.rollerblades;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.WalkAnimationState;

/**
 * Changes the WalkAnimation to support positionScales that vary over time, and add extra fields
 */
public class OverwrittenWalkAnimationState extends WalkAnimationState {
    protected float speedOld;
    protected float speed;
    protected float position;
    protected float positionScale;
    /**
     * a higher sweep value means limbs will linger at the extremes more, and go through 0 faster<br>
     * See {@link com.streetart.client.mixin.rollerblades.HumanoidModelMixin}
     */
    protected float legSweep;

    @Override
    public void setSpeed(final float speed) {
        this.speed = speed;
    }

    @Override
    public void update(final float targetSpeed, final float factor, final float positionScale) {
        this.speedOld = this.speed;
        this.speed += (targetSpeed - this.speed / positionScale) * factor;
        this.position += this.speed;
        this.positionScale = positionScale;
    }

    public void setLegSweep(final float legSweep) {
        this.legSweep = legSweep;
    }

    public float getLegSweep() {
        return this.legSweep;
    }

    @Override
    public void stop() {
        this.speedOld = 0.0f;
        this.speed = 0.0f;
        this.position = 0.0f;
    }

    @Override
    public float speed() {
        return this.speed;
    }

    @Override
    public float speed(final float partialTicks) {
        return Math.min(Mth.lerp(partialTicks, this.speedOld, this.speed), 1.0f);
    }

    @Override
    public float position() {
        return this.position;
    }

    @Override
    public float position(final float partialTicks) {
        return (this.position - this.speed * (1.0f - partialTicks));
    }

    @Override
    public boolean isMoving() {
        return this.speed > 1.0E-5f * this.positionScale;
    }
}
