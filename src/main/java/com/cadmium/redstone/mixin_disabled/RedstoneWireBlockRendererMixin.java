package com.cadmium.redstone.mixin;

import com.cadmium.redstone.client.RedstoneOverhaulClient;
import com.cadmium.redstone.renderer.RendererPipeline;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for RedstoneWireBlockRenderer to override vanilla rendering
 * with our custom GPU-accelerated rendering.
 */
@Mixin(targets = "net/minecraft/client/render/block/RedstoneWireBlockRenderer")
public abstract class RedstoneWireBlockRendererMixin {
    
    /**
     * Inject into render method to add our custom rendering.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRender(BlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        try {
            RendererPipeline pipeline = RedstoneOverhaulClient.getRendererPipeline();
            if (pipeline.isInitialized() && pipeline.getDebugMode() != RendererPipeline.DebugMode.OFF) {
                // Add instance for rendering
                pipeline.addInstance(entity.getPos(), 15); // Placeholder signal strength
            }
        } catch (Exception e) {
            // Silently fail - fall back to vanilla rendering
        }
    }
}

