package com.streetart.schmoovement;

import com.streetart.AllDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

import java.awt.*;

public class RollerBlades {
    public static double WALKING_SPEED_SOFTCAP = 0.1;
    public static double WALKING_SPEED_CAP = 0.2;

    public static double RUNNING_SPEED_SOFTCAP = 0.2;
    public static double RUNNING_SPEED_CAP = 0.4;


    public static boolean canRollUsing(final ItemStack itemStack, final EquipmentSlot slot) {
        if (!itemStack.has(AllDataComponents.ROLLER_BLADES)) {
            return false;
        }
        final Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && slot == equippable.slot() && !itemStack.nextDamageWillBreak();
    }

    public static boolean canRoll(final LivingEntity livingEntity) {
        if (livingEntity instanceof final Player player) {
            if (player.getAbilities().flying) {
                return false;
            }
        }

        for (final EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (!canRollUsing(livingEntity.getItemBySlot(slot), slot)) continue;
            return true;
        }
        return false;
    }

    public static double getBlockFriction(final float original, final LivingEntity entity) {
        final float baseLoss = 1 - original;
        if (entity.isCrouching()) {
            return 1 - baseLoss * 0.25f;
        }

        final Vec3 delta = entity.getDeltaMovement();
        final float speedSqr = (float) delta.horizontalDistanceSqr();
        if (speedSqr > RUNNING_SPEED_SOFTCAP * RUNNING_SPEED_SOFTCAP) {
            return Math.max(original, 1 - baseLoss * (speedSqr - RUNNING_SPEED_SOFTCAP * RUNNING_SPEED_SOFTCAP));
        }
        return 1;
    }

    /**
     * Returns a modified input vector based on the players velocity, broken into three branches<br>
     * <ul>
     *     <li><code>speed < WALKING_SPEED</code> : unmodified</li>
     *     <li><code>speed > RUNNING_SPEED</code> : can only accelerate backwards</li>
     *     <li>else : lerp between the other two results</li>
     * </ul>
     */
    public static Vec2 handleInput(final Vec2 original, final Player player) {
        final Vec2 norm = original.lengthSquared() > 1 ? original.normalized() : original;
        final Vector2d intent = new Vector2d(norm.x, norm.y);

        final Vector2d vel = new Vector2d(
                player.getDeltaMovement().x,
                player.getDeltaMovement().z
        );

        // transform velocity to be relative to forwards look (vel.x | intent.x)
        final double realYRot = player.getYRot() * (Math.PI / 180);
        final double sin = Math.sin(realYRot);
        final double cos = Math.cos(realYRot);
        vel.set(
                 vel.x * cos + vel.y * sin,
                -vel.x * sin + vel.y * cos
        );
        final double speed = vel.length();

        final double softcap = player.isSprinting() ? RUNNING_SPEED_SOFTCAP : WALKING_SPEED_SOFTCAP;
        final double cap = player.isSprinting() ? RUNNING_SPEED_CAP : WALKING_SPEED_CAP;

        if (speed < softcap) {
            Gizmos.line(
                    player.position(),
                    player.position().add(original.x, 0, original.y),
                    Color.YELLOW.getRGB()
            );
            Gizmos.circle(player.position(), 0.25f, GizmoStyle.stroke(Color.GREEN.getRGB()));
            return original;
        }

        final double dot = intent.dot(vel) / speed;

        final Vector2d capped = new Vector2d(intent);
        if (dot > 0) {
            // trying to move forwards limits to perpendicular vector
            final Vector2d temp = new Vector2d(vel);
            temp.mul(dot / (speed));
            capped.sub(temp);
        }

        // bonus turning
        capped.mul(1 + (speed - softcap) * 1);


        if (speed > cap) {
            Gizmos.circle(player.position(), 0.25f, GizmoStyle.stroke(Color.RED.getRGB()));
            if (norm.length() > 1) {
                capped.mul(original.length() / norm.length());
            }
            return new Vec2((float) capped.x, (float) capped.y);
        }

        final double softCapRatio = (cap - speed) / (cap - softcap);

        intent.mul(softCapRatio);
        capped.mul(1 - softCapRatio);
        capped.add(intent);

        if (norm.length() > 1) {
            capped.mul(original.length() / norm.length());
        }
        Gizmos.circle(player.position(), 0.25f, GizmoStyle.stroke(Color.ORANGE.getRGB()));
        Gizmos.line(
                player.position(),
                player.position().add(capped.x, 0, capped.y),
                Color.YELLOW.getRGB()
        );
        return new Vec2((float) capped.x, (float) capped.y);
    }
}
