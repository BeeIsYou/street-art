package com.streetart.mixin.rollerblades;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.streetart.mixinterface.IHasRollerbladeController;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
    @WrapOperation(method = "moveRelative", at = @At(value = "INVOKE", target = "getInputVector"))
    private Vec3 streetArt$transformAcceleration(final Vec3 input, final float speed,
                                                 final float yRot, final Operation<Vec3> operation) {
        final Vec3 accel = operation.call(input, speed, yRot);
        if (this instanceof final IHasRollerbladeController controller) {
            if (controller.getController().isActive()) {
                final Vector2d transformed = controller.getController().transformVelocity(
                        new Vector2d(input.x, input.z),
                        new Vector2d(accel.x, accel.z),
                        speed
                );
                controller.getController().debugFinalAccel = new Vector2d(transformed);
                return new Vec3(
                        transformed.x, accel.y, transformed.y
                );
            }
        }
        return accel;
    }
}
