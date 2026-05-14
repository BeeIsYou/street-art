package com.streetart.client.rendering;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

public class DynamicMippedTexture extends AbstractTexture implements Dumpable {
    private static final Logger LOGGER = LogUtils.getLogger();
    // 1 (full texture) + N mip textures
    public final int textureCount;
    public final int mipCount;
    private final NativeImage[] mipPixels;
    private final GpuTextureView[] mipViews;
    private int baseWidth;
    private int baseHeight;

    private DynamicMippedTexture(final int textureCount) {
        /*this.sampler = RenderSystem.getSamplerCache().getSampler(
                AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.NEAREST, FilterMode.LINEAR, true
        );*/
        this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST, true);
        this.textureCount = textureCount;
        this.mipCount = this.textureCount - 1;
        this.mipPixels = new NativeImage[textureCount];
        this.mipViews = new GpuTextureView[textureCount];
    }

    public DynamicMippedTexture(final String label, final int width, final int height, final boolean zero, final int textureCount) {
        this(textureCount);
        final GpuDevice device = RenderSystem.getDevice();
        this.texture = device.createTexture(label, 5, TextureFormat.RGBA8, width, height, 1, this.textureCount);
        this.createTexture(device, zero);
    }

    public DynamicMippedTexture(final Supplier<String> label, final int width, final int height, final boolean zero, final int textureCount) {
        this(textureCount);
        final GpuDevice device = RenderSystem.getDevice();
        this.texture = device.createTexture(label, 5, TextureFormat.RGBA8, width, height, 1, this.textureCount);
        this.createTexture(device, zero);
    }

    private void createTexture(final GpuDevice device, final boolean zero) {
        this.textureView = device.createTextureView(this.texture);
        for (int i = 0; i < this.textureCount; i++) {
            this.mipPixels[i] = new NativeImage(this.texture.getWidth(i), this.texture.getHeight(i), zero);
            if (i == 0) {
                this.mipViews[i] = this.textureView;
            } else {
                this.mipViews[i] = device.createTextureView(this.texture, i, 1);
            }
        }
    }

    public void upload() {
        if (this.texture != null) {
            final CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            for (int i = 0; i < this.mipPixels.length; i++) {
                commandEncoder.writeToTexture(this.texture, this.mipPixels[i],
                        i, 0, 0, 0, this.texture.getWidth(i), this.texture.getHeight(i), 0, 0);
            }
        } else {
            LOGGER.warn("Trying to upload disposed texture {}", this.getTexture().getLabel());
        }
    }

    public void upload(int x, int y, int width, int height) {
        this.assertMip(x, () -> "X coordinate %d is not divisible by %d for mip level %d");
        this.assertMip(y, () -> "Y coordinate %d is not divisible by %d for mip level %d");
        this.assertMip(width, () -> "Width %d is not divisible by %d for mip level %d");
        this.assertMip(height, () -> "Height %d is not divisible by %d for mip level %d");
        if (this.texture != null) {
            final CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            for (int i = 0; i < this.mipPixels.length; i++) {
                commandEncoder.writeToTexture(this.texture, this.mipPixels[i],
                        i, 0, x, y, width, height, x, y);
                x >>= 1;
                y >>= 1;
                width >>= 1;
                height >>= 1;
            }
        } else {
            LOGGER.warn("Trying to upload disposed texture {}", this.getTexture().getLabel());
        }
    }

    private void assertMip(final int v, final Supplier<String> format) throws IllegalStateException {
        if ((v >>> this.mipCount) << this.mipCount != v) {
            throw new IllegalStateException(String.format(format.get(), v, 1 << this.mipCount, this.mipCount));
        }
    }

    public NativeImage getBasePixels() {
        return this.mipPixels[0];
    }

    public NativeImage[] getPixels() {
        return this.mipPixels;
    }

    @Override
    public void close() {
        for (final GpuTextureView mipView : this.mipViews) {
            mipView.close();
        }
        for (final NativeImage mipPixel : this.mipPixels) {
            mipPixel.close();
        }
        super.close();
    }

    @Override
    public void dumpContents(final Identifier selfId, final Path dir) throws IOException {
        if (!this.mipPixels[0].isClosed()) {
            final String outputId = selfId.toDebugFileName() + ".png";
            final Path path = dir.resolve(outputId);
            this.mipPixels[0].writeToFile(path);
        }
    }
}
