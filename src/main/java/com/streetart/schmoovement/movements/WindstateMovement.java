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

        if (vel.length() < 0.25) {
            return impulse;
        }

        if (impulse.length() == 0) {
            return impulse.zero();
        }

        /*if (impulse.dot(vel) <= 0) {
            return impulse.zero();
        }*/

        final double velAngle = Math.atan2(vel.y, vel.x);
        final double newAngle = Math.atan2(impulse.y, impulse.x);

        // Every day I am reminded that there are languages that used signed modulus, I die inside more
        double deltaAngle = ((((newAngle - velAngle + Math.PI) % Math.TAU) + Math.TAU) % Math.TAU) - Math.PI;
        final double strengthDecay = Math.clamp(this.timer / 10d - 1, 0, 1);
        double dot = Math.clamp(vel.dot(impulse) / (impulse.length() * vel.length()) * 2, 0, 1);
        if (this.bias.length() > 0) {
            dot *= Math.clamp(vel.dot(this.bias) / (this.bias.length() * vel.length()) * 2, 0, 1);
        }
        final double strength = dot * strengthDecay * 0.25;

        deltaAngle = Math.clamp(deltaAngle, -strength, strength);

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
