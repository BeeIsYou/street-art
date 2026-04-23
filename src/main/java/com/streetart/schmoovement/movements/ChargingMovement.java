package com.streetart.schmoovement.movements;

import com.streetart.schmoovement.RollerbladeController;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2d;
import org.joml.Vector3d;

public class ChargingMovement extends Movement {
    public ChargingMovement(final RollerbladeController controller, final LivingEntity owner) {
        super(controller, owner);
    }

    @Override
    public boolean canContinue() {
        return this.controller.coyoteTime() && this.owner.isCrouching();
    }

    @Override
    public void start() {
//        this.controller.crouchTicks = 0;
    }

    @Override
    public void end() {
        if (!this.owner.isCrouching()) {
            final Vector2d vel = this.getVelocityOverwrite();
            final Vector2d delta = new Vector2d(
                    this.owner.getDeltaMovement().x - vel.x,
                    this.owner.getDeltaMovement().z - vel.y
            );
            this.spawnBoostParticles(delta);
            this.owner.setDeltaMovement(vel.x, this.owner.getDeltaMovement().y, vel.y);
            this.controller.crouchTicks = 0;
        }
    }

    private void spawnBoostParticles(final Vector2d deltaVel) {
        this.spawnBlockParticles(
                new Vector3d(deltaVel.x * 5, 0, deltaVel.y * 5),
                new Vector3d(this.owner.position().x, this.owner.position().y + 0.1, this.owner.position().z),
                this.owner.getOnPosLegacy(),
                10
        );
    }

    private Vector2d getVelocityOverwrite() {
        // https://www.desmos.com/calculator/faz4mdmkwz
        // basically. boost towards vel adds. boost away from vel resets
        final Vector2d input = this.getXZInput();
        final Vector2d vel = new Vector2d(
                this.owner.getDeltaMovement().x,
                this.owner.getDeltaMovement().z
        );

        final double boostFrac = Math.min(this.controller.crouchTicks / 10f, 1);
        if (input.length() == 0) {
            return vel.mul(1 - boostFrac);
        }

        final Vector2d boost = input.normalize(boostFrac * 0.2);
        final Vector2d add = vel.add(boost, new Vector2d());
        double dot = boost.dot(add);
        dot = Math.clamp(2 * dot, 0, boost.length() * add.length());
        final double projDot = Math.max(0, dot / boost.lengthSquared());
        boost.mul(projDot, add);
        if (add.length() > this.controller.accelCapEnd) {
            add.normalize(this.controller.accelCapEnd);
        }
        if (add.length() > boost.length()) {
            return add;
        } else {
            return boost;
        }
    }

    @Override
    public Vector2d transformAcceleration(final Vector2d input, final Vector2d impulse) {
        return input.zero();
    }

    @Override
    public String toString() {
        return "Charging";
    }
}
