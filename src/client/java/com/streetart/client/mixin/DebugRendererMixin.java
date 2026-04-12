package com.streetart.client.mixin;

import com.streetart.client.debug.AllDebugEntries;
import com.streetart.client.debug.ReachingOverSidesRenderer;
import com.streetart.client.debug.SprayPaintBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Shadow
    List<DebugRenderer.SimpleDebugRenderer> renderers;

    @Inject(method = "refreshRendererList", at = @At("TAIL"))
    private void streetArt$addRenderers(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.debugEntries.isCurrentlyEnabled(AllDebugEntries.SPRAY_PAINT_BLOCKS)) {
            this.renderers.add(new SprayPaintBlockRenderer());
        }
        if (minecraft.debugEntries.isCurrentlyEnabled(AllDebugEntries.SERVER_SPRAY_PAINT_BLOCKS)) {
            this.renderers.add(new ReachingOverSidesRenderer());
        }
    }
}
