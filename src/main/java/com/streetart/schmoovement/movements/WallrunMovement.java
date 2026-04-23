package com.streetart.schmoovement.movements;

import com.streetart.mixin.LivingEntityInvoker;
import com.streetart.schmoovement.RollerbladeController;
import com.streetart.schmoovement.WallCollideStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2d;
import org.joml.Vector3d;

public class WallrunMovement extends Movement {
    private final WallCollideStatus status;
    private final WindstateMovement windstate;

    public WallrunMovement(final RollerbladeController controller, final LivingEntity owner, final WindstateMovement windstate) {
        super(controller, owner);
        this.status = controller.getWallCollideStatus();
        this.windstate = windstate;
    }

    @Override
    public boolean canContinue() {
        return this.status.getCachedType().isColliding();
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
        return original * 0.25;
    }

    @Override
    public boolean canJump(final boolean onGround) {
        return true;
    }

    @Override
    public Vector3d modifyJump(final Vector3d newVelocity) {
        final double speed = Math.sqrt(newVelocity.dot(newVelocity.x, 0, newVelocity.z));
        newVelocity.fma(speed * 0.25 + 0.25, this.status.getCachedType().normal);
        this.spawnWallrunParticles(10);
        this.controller.transitionTo(this.windstate);
        return newVelocity;
    }

    @Override
    public boolean mayFly() {
        return false;
    }
}
