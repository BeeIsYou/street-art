package com.streetart.schmoovement.movements;

import com.streetart.mixin.LivingEntityInvoker;
import com.streetart.schmoovement.RollerbladeController;
import com.streetart.schmoovement.WallCollideStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2d;
import org.joml.Vector3d;

public class WallrunMovement extends Movement {
    private int uses = 3;
    private boolean lastKicked = false;
    private final WallCollideStatus status;
    /**
     * Enters windstate after a wallrun
     */
    private WindstateMovement windstate;

    public WallrunMovement(final RollerbladeController controller, final LivingEntity owner) {
        super(controller, owner);
        this.status = controller.getWallCollideStatus();
    }

    public void linkWindstate(final WindstateMovement windstate) {
        this.windstate = windstate;
    }

    public void resetUses() {
        this.uses = 3;
        this.lastKicked = false;
    }

    @Override
    public boolean canContinue() {
        return this.status.getCachedType().isColliding() && this.uses > -1;
    }

    @Override
    public void start() {
        if (this.owner.getDeltaMovement().y < 0) {
            this.owner.setDeltaMovement(
                    this.owner.getDeltaMovement().x,
                    this.owner.getDeltaMovement().y * 0.25,
                    this.owner.getDeltaMovement().z
            );
        } else {
            final double maxJumpSpeed = ((LivingEntityInvoker)this.owner).invokeGetJumpPower() * 0.4;
            if (this.owner.getDeltaMovement().y > maxJumpSpeed) {
                this.owner.setDeltaMovement(
                        this.owner.getDeltaMovement().x,
                        maxJumpSpeed,
                        this.owner.getDeltaMovement().z
                );
            }
        }
    }

    @Override
    public void end() {

    }

    private void spawnWallrunParticles(int amount) {
        this.spawnBlockParticles(
                new Vector3d(this.status.getCachedType().normal).mul(0.1).add(
                        -this.owner.getDeltaMovement().x,
                        1.5,
                        -this.owner.getDeltaMovement().z
                ),
                new Vector3d(this.status.getCachedType().normal).mul(0.1).add(
                        this.owner.position().x,
                        this.owner.position().y,
                        this.owner.position().z
                ),
                BlockPos.containing(
                        this.owner.position().x - this.status.getCachedType().normal.x() * (0.1 + this.owner.getBbWidth()),
                        this.owner.position().y - this.status.getCachedType().normal.y() * 0.1,
                        this.owner.position().z - this.status.getCachedType().normal.z() * (0.1 + this.owner.getBbWidth())
                ),
                amount
        );
    }

    @Override
    public Vector2d transformAcceleration(final Vector2d input, final Vector2d impulse) {
        this.spawnWallrunParticles(1);
        return impulse.zero();
    }

    @Override
    public String toString() {
        return "Wallrun";
    }

    @Override
    public double modifyGravity(final double original) {
        return (this.uses > 0 && !this.lastKicked) ? original * 0.25 : original;
    }

    @Override
    public boolean canJump(final boolean onGround) {
        return true;
    }

    @Override
    public Vector3d modifyJump(final Vector3d newVelocity) {
        this.uses--;
        final double speed = Math.sqrt(newVelocity.dot(newVelocity.x, 0, newVelocity.z)) * 0.25 + 0.25;
        if (this.uses < 0) {
            newVelocity.mul(0.5, 1, 0.5);
            if (this.windstate != null) {
                this.controller.transitionTo(this.windstate);
            }
        } else if (this.owner.isCrouching() && !this.lastKicked) {
            newVelocity.mul(0.75, 1, 0.75);
            this.lastKicked = true;
        } else {
            newVelocity.fma(speed, this.status.getCachedType().normal);
            if (this.windstate != null) {
                this.controller.transitionTo(this.windstate);
            }
            this.lastKicked = false;
        }

        this.spawnWallrunParticles(10);

        return newVelocity;
    }

    @Override
    public boolean mayFly() {
        return false;
    }
}
