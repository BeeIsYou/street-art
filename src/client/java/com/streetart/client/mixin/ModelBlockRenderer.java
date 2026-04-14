package com.streetart.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.streetart.client.StreetArtClient;
import com.streetart.client.manager.GClientBlock;
import com.streetart.client.rendering.LightMath;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRenderer {
    @Inject(method = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getOffset(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;")
    )
    private void streetArt$updatePaintLights(final CallbackInfo ci,
                                             @Local(argsOnly = true) final BlockAndTintGetter level,
                                             @Local(argsOnly = true) final BlockPos pos,
                                             @Local(argsOnly = true) final BlockState blockState) {
        GClientBlock block = StreetArtClient.textureManager.getGraffiti().get(pos);
        if (block != null) {
            block.forEach(data -> LightMath.OhGodSoMuchMath(data, level, blockState));
        }
    }
}
