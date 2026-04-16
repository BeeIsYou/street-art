package com.streetart.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.streetart.client.StreetArtClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow
    private LevelTargetBundle targets;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "addLateDebugPass"))
    private void streetArt$updateSprayPaint(final CallbackInfo ci, @Local final FrameGraphBuilder frame) {
        final FramePass pass = frame.addPass("street_art:spray_paint_upload");
        this.targets.main = pass.readsAndWrites(this.targets.main);
        pass.executes(() -> {
            StreetArtClient.tileAtlasManager.checkDirty();
        });
    }
}
