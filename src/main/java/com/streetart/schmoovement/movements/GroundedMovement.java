package com.streetart.schmoovement.movements;

import com.streetart.schmoovement.RollerbladeController;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2d;

public class GroundedMovement extends Movement {
    /**
     * Resets efficacy of wallrunning
     */
    private WallrunMovement wallrun;

    public GroundedMovement(final RollerbladeController controller, final LivingEntity owner) {
        super(controller, owner);
    }

    public void linkWallrun(final WallrunMovement wallrun) {
        this.wallrun = wallrun;
    }

    @Override
    public boolean canContinue() {
        return this.owner.onGround();
    }

    @Override
    public void start() {
        if (this.wallrun != null) {
            this.wallrun.resetUses();
        }
    }

    @Override
    public void end() {

    }

    @Override
    public Vector2d transformAcceleration(final Vector2d input, final Vector2d impulse) {
        final Vector2d vel = new Vector2d(
                this.owner.getDeltaMovement().x,
                this.owner.getDeltaMovement().z
        );

        // no zero div :p
        if (impulse.length() < 1e-5 || vel.length() < 1e-5) {
            return impulse;
        }

        // 1 = accelerating into vel
        // 0 = accelerating perpendicular to vel
        // -1 = accelerating away from vel
        final double dot = impulse.dot(vel) / (impulse.length() * vel.length());

        if (dot > 0) {
            // sin(t)^4 , trying to emulate pushing with each leg
            double stride = Math.sin(this.controller.stride * Math.TAU * 0.04);
            stride *= stride;
            double mul = stride;
            mul = 1 - (1 - mul) * dot;
            impulse.mul(mul);
        }

        if (vel.length() < this.controller.accelCapStart) {
            return impulse;
        }

        final Vector2d capped = RollerbladeController.scaleBackToPerpendicular(impulse, vel, new Vector2d(), 1);


        if (vel.length() > this.controller.accelCapEnd) {
            return capped;
        }

        double capRatio = this.controller.getCapScalar(vel.length());
        capRatio = Math.sqrt(capRatio);

        impulse.mul(1 - capRatio);
        capped.mul(capRatio);
        capped.add(impulse);

        return capped;
    }

    @Override
    public String toString() {
        return "Grounded";
    }
}
