package com.streetart.schmoovement.movements;

import com.streetart.schmoovement.RollerbladeController;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class WindstateMovement extends Movement {
    private int timer = 0;
    private Vector2dc bias = new Vector2d();
    public WindstateMovement(final RollerbladeController controller, final LivingEntity owner) {
        super(controller, owner);
    }

    @Override
    public boolean canContinue() {
        return this.timer > 0;
    }

    @Override
    public void start() {
        this.bias = new Vector2d(
                this.controller.getWallCollideStatus().getCachedType().normal.x(),
                this.controller.getWallCollideStatus().getCachedType().normal.z()
        );
        this.timer = 20;
        this.controller.getWallCollideStatus().resetToNone();
    }

    @Override
    public void end() {
        this.timer = 0;
    }

    @Override
    public Vector2d transformAcceleration(final Vector2d input, final Vector2d impulse) {
        this.timer --;

        final Vector2d vel = new Vector2d(this.owner.getDeltaMovement().x, this.owner.getDeltaMovement().z);

        if (impulse.dot(vel) < 0) {
            return impulse.zero();
        }

        final Vector2d newVel = impulse.mul(10).add(vel);
        final double velAngle = Math.atan2(vel.y, vel.x);
        final double newAngle = Math.atan2(newVel.y, newVel.x);
        final double biasAngle = Math.atan2(this.bias.y(), this.bias.x());

        double deltaAngle = ((newAngle - velAngle - Math.PI) % Math.TAU) + Math.PI;
        final double strength = Math.clamp(this.timer / 10d - 1, 0, 1) * 0.2;
        deltaAngle = Math.clamp(deltaAngle, -strength, strength);

        if (this.bias.length() > 1E-5) {
            if (Math.abs(velAngle - biasAngle + deltaAngle) > Math.abs(velAngle - biasAngle)) {
                // rotating away from bias
                final double biasAdjust = Math.max(0, this.bias.dot(newVel) / newVel.length());
                deltaAngle *= biasAdjust;
            }
        }

        final double cos = Math.cos(deltaAngle);
        final double sin = Math.sin(deltaAngle);

        vel.set(
                vel.x * cos - vel.y * sin,
                vel.y * cos + vel.x * sin
        );

        this.owner.setDeltaMovement(
                vel.x,
                this.owner.getDeltaMovement().y,
                vel.y
        );

        return impulse.zero();
    }

    @Override
    public String toString() {
        return "Windstate";
    }

    @Override
    public double modifyGravity(final double original) {
        return original * 0.75;
    }

    @Override
    public boolean mayFly() {
        return false;
    }
}
