package com.streetart.schmoovement;

import com.streetart.AllDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.joml.Vector2d;
import org.joml.Vector4f;

public class RollerbladeController {
    private final LivingEntity owner;

    private boolean active = false;
    private boolean wasCrouching = false;
    private int crouchingTicks = 0;
    private int airTicks = 0;
    private int stride = 0;
    private final double accelSpeed = 0.1;
    private final double accelCapStart = 0.3;
    private final double accelCapEnd = 0.45;

    public RollerbladeController(final LivingEntity owner) {
        this.owner = owner;
    }

    public boolean isActive() {
        return this.active;
    }

    public static boolean canRollUsing(final ItemStack itemStack, final EquipmentSlot slot) {
        if (!itemStack.has(AllDataComponents.ROLLER_BLADES)) {
            return false;
        }
        final Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && slot == equippable.slot() && !itemStack.nextDamageWillBreak();
    }

    public boolean canRoll() {
        if (this.owner instanceof final Player player) {
            if (player.getAbilities().flying) {
                return false;
            }
        }

        for (final EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (!canRollUsing(this.owner.getItemBySlot(slot), slot)) continue;
            return true;
        }
        return false;
    }

    public void tickActive() {
        this.active = this.canRoll();
        if (this.wasCrouching) {
            this.crouchingTicks++;
        } else {
            this.crouchingTicks = 0;
        }
        this.wasCrouching = this.owner.isCrouching();

        if (this.owner.onGround()) {
            this.airTicks = 0;
        } else {
            this.airTicks++;
        }

        if (this.owner.xxa != 0 || this.owner.zza != 0) {
            this.stride++;
        } else {
            this.stride = 0;
        }

        this.debugCrouchingTicks = this.crouchingTicks;
        this.debugStride = this.stride;
    }

    public double getBlockFriction(final float original) {
        final double speed = this.owner.getDeltaMovement().horizontalDistance();
        if (this.owner.xxa == 0 && this.owner.zza == 0) {
            if (speed < 0.05) {
                return original;
            }
            if (this.owner.isCrouching()) {
                return 1 - (1 - original) * 0.1;
            }
        }
        if (speed > 1) {
            return 0.99;
        }
        return 1;
    }

    public double getCapScalar(final double speed) {
        return (speed - this.accelCapStart) / (this.accelCapEnd - this.accelCapStart);
    }

    public Vector2d debugOriginalAccel = new Vector2d();
    public Vector2d debugCappedAccel = new Vector2d();
    public Vector2d debugFinalAccel = new Vector2d();
    public double debugCapRatio = 0;
    public int debugCrouchingTicks = 0;
    public double debugStride = 0;

    public Vector2d transformVelocity(final Vector2d input, final Vector2d impulse, final float entitySpeed) {
        this.debugOriginalAccel = new Vector2d(impulse);
        this.debugCappedAccel = new Vector2d();
        this.debugStride = 0;

        // noop while crouching :)
        if (this.owner.isCrouching()) {
            return new Vector2d(0, 0);
        }

        final Vector2d vel = new Vector2d(
                this.owner.getDeltaMovement().x,
                this.owner.getDeltaMovement().z
        );

        final boolean performCrouchBoost = this.crouchingTicks > 0 && this.airTicks < 4;

        // perform a crouch boost
        if (performCrouchBoost) {
            final double boost = Math.min(this.crouchingTicks * 1d, 10);
            final Vector2d fromZero = new Vector2d(impulse).mul(boost);
            final Vector2d fromVel = new Vector2d(impulse).mul(boost).add(vel);
            if (fromZero.length() > fromVel.length()) {
                impulse.set(fromZero).sub(vel);
            } else {
                impulse.set(fromVel).sub(vel);
            }
            this.crouchingTicks = 0;
        }

        // no zero div :p
        if (impulse.length() < 1e-5 || vel.length() < 1e-5) {
            return impulse;
        }

        // 1 = accelerating into vel
        // 0 = accelerating perpendicular to vel
        // -1 = accelerating away from vel
        final double dot = impulse.dot(vel) / (impulse.length() * vel.length());

        if (!performCrouchBoost && dot > 0) {
            // sin(t)^4 , trying to emulate pushing with each leg
            double stride = Math.sin(this.stride * Math.TAU * 0.04);
            stride *= stride;
            this.debugStride = stride;
            double mul = stride;
            mul = 1 - (1 - mul) * dot;
            impulse.mul(mul);
            impulse.mul(0.25);
        }

        if (vel.length() < this.accelCapStart) {
            return impulse;
        }

        final Vector2d capped = new Vector2d(impulse);

        if (dot > 0) {
            // d = v * a / (|v|^2)
            // cap = a - v * d
            final double scalar = dot * impulse.length() / vel.length();
            // trying to move forwards limits to perpendicular vector
            final Vector2d temp = new Vector2d(vel);
            temp.mul(scalar);
            capped.sub(temp);
        }

        this.debugCappedAccel = new Vector2d(capped);

        if (vel.length() > this.accelCapEnd) {
            return capped;
        }

        final double capRatio = this.getCapScalar(vel.length());
        this.debugCapRatio = capRatio;

        impulse.mul(1 - capRatio);
        capped.mul(capRatio);
        capped.add(impulse);

        return capped;
    }

    private void rotateInputToLook(final Vector2d input) {
        // transform input to be relative to forwards look
        final double realYRot = -this.owner.getYRot() * (Math.PI / 180);
        final double sin = Math.sin(realYRot);
        final double cos = Math.cos(realYRot);
        input.set(
                input.x * cos + input.y * sin,
                -input.x * sin + input.y * cos
        );
    }

    /**
     * Is injected into where movement speed affects fov (currentSpeed / walkSpeed)
     * @param baseWalk base walk speed - denominator
     * @param currentMove current move speed - numerator
     * @return multiplier to ratio
     */
    public double getFovScalar(final double baseWalk, final double currentMove) {
        double scalar = 1;
        final double speed = this.owner.getDeltaMovement().horizontalDistance();
        if (speed > this.accelCapStart) {
            final double cap = Math.min(1, this.getCapScalar(speed));
            scalar += cap / 4;
        }
        scalar *= 1 - (Math.min(this.crouchingTicks, 10) / 40d);
        return scalar;
    }

    public Vector4f getAnimationModifiers() {
        return new Vector4f(1, 1, 1, 1);
    }
}
