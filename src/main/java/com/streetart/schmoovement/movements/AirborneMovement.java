package com.streetart.schmoovement.movements;

import com.streetart.schmoovement.RollerbladeController;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2d;

public class AirborneMovement extends GroundedMovement {
    public AirborneMovement(final RollerbladeController controller, final LivingEntity owner) {
        super(controller, owner);
    }

    @Override
    public boolean canContinue() {
        return !this.controller.coyoteTime();
    }

    @Override
    public Vector2d transformAcceleration(final Vector2d input, Vector2d impulse) {
        impulse = super.transformAcceleration(input, impulse);

        final Vector2d vel = new Vector2d(this.owner.getDeltaMovement().x, this.owner.getDeltaMovement().z);
        final double angle = vel.angle(vel);
        final double cos = Math.cos(angle) * 0.1; // should limit forwards/backwards acceleration
        final double sin = Math.sin(angle); // but leave strafing untouched

        impulse.set(
                impulse.x * cos + impulse.y * sin,
                impulse.y * cos - impulse.x * sin
        );

        return impulse;
    }

    @Override
    public String toString() {
        return "Airborne";
    }
}
