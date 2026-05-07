package com.streetart.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.streetart.mixinterface.IHasRollerbladeController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @WrapOperation(method = "moveRelative", at = @At(value = "INVOKE", target = "getInputVector"))
    private Vec3 streetArt$transformAcceleration(final Vec3 input, final float speed,
                                                 final float yRot, final Operation<Vec3> operation) {
        final Vec3 accel = operation.call(input, speed, yRot);
        if (this instanceof final IHasRollerbladeController controller && !((Object)this instanceof ServerPlayer)) {
            if (controller.getController().isActive()) {
                final Vector2d transformed = controller.getController().transformAcceleration(
                        new Vector2d(input.x, input.z),
                        new Vector2d(accel.x, accel.z)
                );
                return new Vec3(
                        transformed.x, accel.y, transformed.y
                );
            }
        }
        return accel;
    }

    @Inject(method = "collide", at = @At(value = "RETURN"))
    private void streetArt$markWallCollide(final Vec3 movement, final CallbackInfoReturnable<Vec3> ci) {
        if (this instanceof final IHasRollerbladeController controller) {
            controller.getController().getWallCollideStatus().markCollision(movement, ci.getReturnValue());
        }
    }

    @WrapOperation(method = "collide", at = @At(value = "INVOKE", target = "onGround"))
    private boolean streetArt$rollerbladeAirStepUp(final Entity instance, final Operation<Boolean> operation) {
        return operation.call(instance) ||
                (this instanceof final IHasRollerbladeController controller && controller.getController().isZooming());
    }

    @WrapOperation(method = "getGravity", at = @At(value = "INVOKE", target = "getDefaultGravity"))
    private double streetArt$rollerbladeModifyGravity(final Entity instance, final Operation<Double> operation) {
        final double original = operation.call(instance);
        if (this instanceof final IHasRollerbladeController controller && controller.getController().isActive()) {
            return controller.getController().currentMovement.modifyGravity(original);
        }
        return original;
    }

	@ModifyReturnValue(method = "canSpawnSprintParticle", at = @At("RETURN"))
	private boolean streetArt$preventSprintParticle(boolean original) {
		if (original && this instanceof IHasRollerbladeController controller && controller.getController().isActive()) {
			return false;
		}
		return original;
	}
}
