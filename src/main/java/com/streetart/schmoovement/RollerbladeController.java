package com.streetart.schmoovement;

import com.streetart.AllDataComponents;
import com.streetart.StreetArt;
import com.streetart.schmoovement.movements.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector4f;

import java.util.List;
import java.util.function.Function;

public class RollerbladeController {
    private final LivingEntity owner;

    private boolean active = false;

    private int uncrouchTicks = 0;
    public int crouchTicks = 0;
    public int airTicks = 0;
    public int stride = 0;

    private final WallCollideStatus wallCollideStatus = new WallCollideStatus();

    public final double accelCapStart = 0.15;
    public final double stepBonus = 0.30;
    public final double accelCapEnd = 0.45;

    private final List<Movement> movements;
    public Movement currentMovement;

    private static final Identifier STEP_MODIFIER_ZOOMING_ID = StreetArt.id("step_height_zooming");
    private static final AttributeModifier STEP_MODIFIER_ZOOMING = new AttributeModifier(
            STEP_MODIFIER_ZOOMING_ID, 0.5F, AttributeModifier.Operation.ADD_VALUE
    );

    public static RollerbladeController simple(final LivingEntity owner) {
        return new RollerbladeController(owner, c -> List.of(
                new GroundedMovement(c, owner)
        ));
    }

    public static RollerbladeController advanced(final LivingEntity owner) {
        return new RollerbladeController(owner, c -> {
            final ChargingMovement charging = new ChargingMovement(c, owner);
            final GroundedMovement grounded = new GroundedMovement(c, owner);
            final WallrunMovement wallrun = new WallrunMovement(c, owner);
            final WindstateMovement windstate = new WindstateMovement(c, owner);
            final AirborneMovement airborne = new AirborneMovement(c, owner);

            charging.linkWallrun(wallrun);
            grounded.linkWallrun(wallrun);
            wallrun.linkWindstate(windstate);

            return List.of(charging, grounded, wallrun, windstate, airborne);
        });
    }

    public RollerbladeController(final LivingEntity owner, final Function<RollerbladeController, List<Movement>> movements) {
        this.owner = owner;
        this.movements = movements.apply(this);
        this.currentMovement = this.movements.getLast();
    }

    public boolean coyoteTime() {
        return this.airTicks < 4;
    }

    public WallCollideStatus getWallCollideStatus() {
        return this.wallCollideStatus;
    }

    public void alwaysTick() {
        this.active = this.canRoll();
    }

    public boolean isActive() {
        return this.active && !this.owner.onClimbable() && !this.owner.isSwimming();
    }

    /**
     * Moving fast enough for "zooming" tricks. e.g. bonus step height
     */
    public boolean isZooming() {
        return this.isActive() && this.owner.getDeltaMovement().horizontalDistanceSqr() > this.stepBonus * this.stepBonus;
    }

    public void transitionTo(final Movement movement) {
        this.currentMovement.end();
        this.currentMovement = movement;
        this.currentMovement.start();
    }

    public void preMove() {
        if (!this.isActive()) {
            this.currentMovement.end();
            this.currentMovement = this.movements.getLast();
            this.currentMovement.start();
        }
        for (final Movement movement : this.movements) {
            if (movement.canContinue()) {
                if (movement != this.currentMovement) {
                    this.transitionTo(movement);
                }
                break;
            }
        }

        final AttributeInstance step = this.owner.getAttribute(Attributes.STEP_HEIGHT);
        step.removeModifier(STEP_MODIFIER_ZOOMING.id());
        if (this.isZooming()) {
            step.addTransientModifier(STEP_MODIFIER_ZOOMING);
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

        this.wallCollideStatus.testCache(this.owner);
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
        return new Vector4f(1f, 0.25f, 0.25f, 2.5f);
    }
}
