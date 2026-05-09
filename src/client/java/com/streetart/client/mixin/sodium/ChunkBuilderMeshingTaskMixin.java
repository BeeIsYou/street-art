package com.streetart.client.mixin.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import com.streetart.client.StreetArtClient;
import com.streetart.client.manager.GClientBlock;
import com.streetart.client.manager.GClientManager;
import com.streetart.client.rendering.LightMath;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public class ChunkBuilderMeshingTaskMixin {
    @Inject(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z")
    )
    private void streetArt$sodiumCompilePaint(final ChunkBuildContext buildContext, final CancellationToken cancellationToken, final CallbackInfoReturnable<ChunkBuildOutput> cir,
                                              @Local(name = "slice") final LevelSlice region,
                                              @Local(name = "blockPos") final BlockPos.MutableBlockPos pos,
                                              @Local(name = "blockState") final BlockState blockState,
                                              @Local(name = "y") final int y,
                                              @Local(name = "z") final int z,
                                              @Local(name = "x") final int x
    ) {
        StreetArtClient.layers.forEach((_, atlas) -> {
            final GClientManager manager = atlas.get(x, z);

            if (manager != null) {
                final LightMath math = new LightMath();
                pos.set(x, y, z);
                final GClientBlock block = manager.getGraffiti().get(pos);
                if (block != null) {
                    block.forEach(data -> math.OhGodSoMuchMath(data, region, blockState));
                }
            }
        });
    }
}
