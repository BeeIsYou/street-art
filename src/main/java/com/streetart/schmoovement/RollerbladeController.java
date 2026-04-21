package com.streetart.schmoovement;

import com.streetart.AllDataComponents;
import com.streetart.schmoovement.movements.AirborneMovement;
import com.streetart.schmoovement.movements.ChargingMovement;
import com.streetart.schmoovement.movements.GroundedMovement;
import com.streetart.schmoovement.movements.Movement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector4f;

import java.util.List;

public class RollerbladeController {
    private final LivingEntity owner;

    private boolean active = false;

    private int uncrouchTicks = 0;
    public int crouchTicks = 0;
    public int airTicks = 0;
    public int stride = 0;

    public final double accelCapStart = 0.15;
    public final double accelCapEnd = 0.45;

    private final List<Movement> movements;
    public Movement currentMovement;

    public RollerbladeController(final LivingEntity owner) {
        this.owner = owner;
        this.movements = List.of(
                new ChargingMovement(this, this.owner),
                new GroundedMovement(this, this.owner),
                new AirborneMovement(this, this.owner)
        );
        this.currentMovement = this.movements.getLast();
    }

    public boolean coyoteTime() {
        return this.airTicks < 4;
    }

    public void alwaysTick() {
        this.active = this.canRoll();
    }

    public void preMove() {
        for (final Movement movement : this.movements) {
            if (movement.canContinue()) {
                if (movement != this.currentMovement) {
                    this.currentMovement.end();
                    this.currentMovement = movement;
                    this.currentMovement.start();
                }
                return;
            }
        }
    }

    public void postMove() {
        if (this.owner.isCrouching()) {
            this.crouchTicks++;
            this.uncrouchTicks = 0;
        } else {
            this.uncrouchTicks++;
        }
        if (this.uncrouchTicks > 5) {
            this.crouchTicks = 0;
        }

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
    }

    public Vector2d transformAcceleration(final Vector2d input, final Vector2d impulse) {
        this.preMove();
        final Vector2d res = this.currentMovement.transformAcceleration(input, impulse);
        this.postMove();
        return res;
    }

    public double modifyBlockFriction(final float original) {
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

    public double getCapScalar(final double speed) {
        return (speed - this.accelCapStart) / (this.accelCapEnd - this.accelCapStart);
    }

    public static Vector2d scaleBackToPerpendicular(final Vector2dc vec, final Vector2dc dir, final Vector2d dest, final double scaleBack) {
        if (scaleBack <= 0) {
            dest.set(vec);
            return dest;
        }
        final double len = dir.length();
        final double dot = -vec.dot(dir) / (len * len);
        if (dot >= 0) {
            dest.set(vec);
            return dest;
        }

        dest.set(dir).mul(dot).add(vec);

        if (scaleBack >= 1) {
            return dest;
        }

        dest.lerp(vec, scaleBack);
        return dest;
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
        return scalar;
    }

    public Vector4f getAnimationModifiers() {
        return new Vector4f(1, 1, 1, 1);
    }
}
