package com.cadmium.redstone.mixin;

import com.cadmium.redstone.client.RedstoneOverhaulClient;
import com.cadmium.redstone.renderer.RendererPipeline;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for WorldRenderer to inject custom redstone rendering into the render pipeline.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    
    /**
     * Inject into render method to add our custom redstone rendering pass.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(Matrix4f matrix, Matrix4f projectionMatrix, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, CallbackInfo ci) {
        try {
            RendererPipeline pipeline = RedstoneOverhaulClient.getRendererPipeline();
            if (pipeline.isInitialized()) {
                MatrixStack matrices = new MatrixStack();
                pipeline.renderDebugOverlay(matrices, camera);
            }
        } catch (Exception e) {
            // Silently fail - don't break vanilla rendering
        }
    }
}

