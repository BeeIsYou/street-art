package com.streetart.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.streetart.AttachmentTypes;
import com.streetart.managers.GServerChunkManager;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    protected ServerLevelMixin(final WritableLevelData levelData, final ResourceKey<Level> dimension, final RegistryAccess registryAccess, final Holder<DimensionType> dimensionTypeRegistration, final boolean isClientSide, final boolean isDebug, final long biomeZoomSeed, final int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Inject(method = "tickChunk(Lnet/minecraft/world/level/chunk/LevelChunk;I)V", at = @At("TAIL"))
    private void streetart$TickManagers(final LevelChunk chunk, final int tickSpeed, final CallbackInfo ci, @Local final ProfilerFiller filler) {
        filler.push("Street Art Chunk Managers");

        final GServerChunkManager manager = chunk.getAttached(AttachmentTypes.CHUNK_MANAGER);
        if (manager != null) {
            final ServerLevel sl = (ServerLevel) (Object) this;
            if (manager.tick(sl, chunk.getPos())) {
                chunk.markUnsaved();
            }
        }

        filler.pop();
    }


}
