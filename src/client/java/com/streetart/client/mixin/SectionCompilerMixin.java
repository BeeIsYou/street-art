package com.streetart.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.streetart.client.StreetArtClient;
import com.streetart.client.manager.GClientBlock;
import com.streetart.client.manager.GClientManager;
import com.streetart.client.rendering.LightMath;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin {
    @Inject(method = "Lnet/minecraft/client/renderer/chunk/SectionCompiler;compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z")
    )
    private void streetArt$compilePaint(final CallbackInfoReturnable<SectionCompiler.Results> cir,
                                        @Local(argsOnly = true) final RenderSectionRegion region,
                                        @Local(ordinal = 2) final BlockPos pos,
                                        @Local final BlockState blockState
    ) {
        final GClientManager manager = StreetArtClient.textureManager.get(ChunkPos.containing(pos));

        if (manager != null) {
            final GClientBlock block = manager.getGraffiti().get(pos);
            if (block != null) {
                block.forEach(data -> LightMath.OhGodSoMuchMath(data, region, blockState));
            }
        }
    }
}
