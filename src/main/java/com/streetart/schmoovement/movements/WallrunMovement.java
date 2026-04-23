package com.streetart.schmoovement.movements;

import com.streetart.mixin.LivingEntityInvoker;
import com.streetart.schmoovement.RollerbladeController;
import com.streetart.schmoovement.WallCollideStatus;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2d;
import org.joml.Vector3d;

public class WallrunMovement extends Movement {
    private final WallCollideStatus status;

    public WallrunMovement(final RollerbladeController controller, final LivingEntity owner) {
        super(controller, owner);
        this.status = controller.getWallCollideStatus();
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

    @Override
    public Vector2d transformAcceleration(final Vector2d input, final Vector2d impulse) {
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
        this.controller.transitionTo(this.controller.windstate);
        return newVelocity;
    }

    @Override
    public boolean mayFly() {
        return false;
    }
}
