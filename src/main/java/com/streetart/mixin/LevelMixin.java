package com.streetart.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.streetart.managers.GLevelManager;
import com.streetart.managers.GraffitiGlobalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Shadow
    abstract BlockState getBlockState(final BlockPos pos);
    @Shadow
    abstract boolean isClientSide();

    @Inject(method = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;")
    )
    private void streetart$storeOldVoxelShape(CallbackInfoReturnable<Boolean> ci,
                                              @Local(argsOnly = true) BlockPos pos,
                                              @Share("oldVoxelShape") LocalRef<VoxelShape> oldVoxelShape
    ) {
        if (!this.isClientSide()) {
            oldVoxelShape.set(this.getBlockState(pos).getShape((Level) (Object) this, pos));
        }
    }

    @Inject(method = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlocksDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V")
    )
    private void streetart$compareVoxelShape(CallbackInfoReturnable<Boolean> ci,
                                             @Local(argsOnly = true) BlockPos pos,
                                             @Local(argsOnly = true) BlockState newState,
                                             @Share("oldVoxelShape") LocalRef<VoxelShape> oldVoxelShape
    ) {
        if ((Object)this instanceof ServerLevel serverLevel) {
            VoxelShape newShape = newState.getShape((Level) (Object) this, pos);
            if (newShape != oldVoxelShape.get()) {
                GLevelManager manager = GraffitiGlobalManager.getGraffitiLevelManager(serverLevel);
                manager.markForRemoval(pos);
                for (Direction dir : Direction.values()) {
                    if (Block.isShapeFullBlock(newShape.getFaceShape(dir))) {
                        manager.markSmothered(pos.relative(dir), dir.getOpposite());
                    }
                }
            }
        }
    }
}
