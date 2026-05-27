package com.streetart.schmoovement.movements;

import com.streetart.schmoovement.RollerbladeController;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2d;
import org.joml.Vector3d;

// todo thoughts:
// change charging to drifting
// releasing can still boost/redirect, but with less efficacy
// holding it doesn't let you accelerate, but lets you turn much more effectively
// zeroing speed happens when going against movement
public class DriftingMovement extends Movement {
    /**
     * Resets efficacy of wallrunning
     */
    private WallrunMovement wallrun;

    private double turning = 0;
    private double bestTurn = 0;

    public DriftingMovement(final RollerbladeController controller, final LivingEntity owner) {
        super(controller, owner);
    }

    public void linkWallrun(final WallrunMovement wallrun) {
        this.wallrun = wallrun;
    }

    @Override
    public boolean canContinue() {
        return this.controller.coyoteTime() && this.owner.isCrouching();
    }

    @Override
    public void start() {
        if (this.wallrun != null) {
            this.wallrun.resetUses();
        }
        this.turning = 0;
        this.bestTurn = 0;
    }

    @Override
    public void end() {
        if (!this.owner.isCrouching()) {
            final double strength = Math.clamp(this.bestTurn / Math.PI * 4 - 1, 0, 1);
            if (strength > 0 && (this.owner.xxa != 0 || this.owner.zza != 0)) {
                final Vector2d vel = new Vector2d(
                        this.owner.getDeltaMovement().x,
                        this.owner.getDeltaMovement().z
                );
                final double l = vel.length();
                vel.mul(1 + strength / l * 0.1);
                this.owner.setDeltaMovement(
                        vel.x,
                        this.owner.getDeltaMovement().y,
                        vel.y
                );
                this.spawnBoostParticles(vel, 10);
            }
            this.controller.crouchTicks = 0;
        }
    }

    private void spawnBoostParticles(final Vector2d deltaVel, int count) {
        this.spawnBlockParticles(
                new Vector3d(deltaVel.x * 5, 0, deltaVel.y * 5),
                new Vector3d(this.owner.position().x, this.owner.position().y + 0.1, this.owner.position().z),
                this.owner.getOnPosLegacy(),
                count
        );
    }

    @Override
    public Vector2d transformAcceleration(final Vector2d input, final Vector2d impulse) {
        final Vector2d vel = new Vector2d(
                this.owner.getDeltaMovement().x,
                this.owner.getDeltaMovement().z
        );

        if (vel.length() <= 0.05) {
            return impulse;
        }

        double deltaAngle = RollerbladeController.deltaAngle (vel, impulse);
        final double maxDeltaAngle = (Math.clamp(this.controller.crouchTicks / 40d, 0, 1) + 0.5) * 0.09;

        deltaAngle = Math.clamp(deltaAngle, -maxDeltaAngle, maxDeltaAngle);
        this.turning += deltaAngle;
        this.bestTurn = Math.max(this.bestTurn, Math.abs(this.turning));

        final double turning = Math.clamp(this.bestTurn / Math.PI * 4 - 1, 0, 1);
        if (turning > 0) {
            this.spawnBoostParticles(vel, turning == 1 ? 4 : 1);
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
        return "Drifting";
    }
}
