package com.streetart.client.rendering.rollerblades;

import com.streetart.misc.OverwrittenWalkAnimationState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class LivingEntityRenderStateExtras {
    public static final RenderStateDataKey<Float> legSweepSpeed = RenderStateDataKey.create(() -> "steet_art:leg_sweep_speed");

    public static void extractSweep(final LivingEntityRenderState state, final OverwrittenWalkAnimationState walkAnimation) {
        state.setData(legSweepSpeed, walkAnimation.getLegSweep());
    }

    public static float getSweep(final LivingEntityRenderState state) {
        return state.getData(legSweepSpeed);
    }
}
