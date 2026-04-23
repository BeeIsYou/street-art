package com.streetart.schmoovement.movements;

import com.streetart.schmoovement.RollerbladeController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2d;
import org.joml.Vector3d;

public abstract class Movement {
    protected final RollerbladeController controller;
    protected final LivingEntity owner;

    public Movement(final RollerbladeController controller, final LivingEntity owner) {
        this.controller = controller;
        this.owner = owner;
    }

    public abstract boolean canContinue();
    public abstract void start();
    public abstract void end();

    /**
     * @param input user input
     * @param impulse current impulse from input
     * @return new impulse
     */
    public abstract Vector2d transformAcceleration(final Vector2d input, final Vector2d impulse);

    @Override
    public abstract String toString();

    protected final Vector2d getXZInput() {
        final float sin = Mth.sin(this.owner.getYRot() * (float) (Math.PI / 180.0));
        final float cos = Mth.cos(this.owner.getYRot() * (float) (Math.PI / 180.0));
        return new Vector2d(
                this.owner.xxa * cos - this.owner.zza * sin,
                this.owner.zza * cos + this.owner.xxa * sin
        );
    }

    protected void spawnBlockParticles(final Vector3d direction, final Vector3d sourcePos, final BlockPos blockSource, final int count) {
        final BlockState blockState = this.owner.level().getBlockState(blockSource);
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            for (int i = 0; i < count; i++) {
                this.owner.level().addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                        sourcePos.x, sourcePos.y, sourcePos.z,
                        direction.x, direction.y, direction.z);
            }
        }
    }

    public double modifyBlockFriction(final float original) {
        return 1;
    }

    public boolean canJump(final boolean onGround) {
        return onGround;
    }

    public Vector3d modifyJump(final Vector3d newVelocity) {
        return newVelocity;
    }

    public double modifyGravity(final double original) {
        return original;
    }

    public boolean mayFly() {
        return true;
    }
}
