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
    public Vector2d transformAcceleration(final Vector2d input, final Vector2d impulse) {
        return super.transformAcceleration(input, impulse).mul(0.1);
    }

    @Override
    public String toString() {
        return "Airborne";
    }
}
