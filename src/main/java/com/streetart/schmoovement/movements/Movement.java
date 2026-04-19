package com.streetart.schmoovement.movements;

import com.streetart.schmoovement.RollerbladeController;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector2d;

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

    public double modifyBlockFriction(final float original) {
        return 1;
    }

    @Override
    public String toString() {
        throw new NotImplementedException();
    }

    protected final Vector2d getXZInput() {
        final float sin = Mth.sin(this.owner.getYRot() * (float) (Math.PI / 180.0));
        final float cos = Mth.cos(this.owner.getYRot() * (float) (Math.PI / 180.0));
        return new Vector2d(
                this.owner.xxa * cos - this.owner.zza * sin,
                this.owner.zza * cos + this.owner.xxa * sin
        );
    }

}
