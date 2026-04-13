package com.cadmium.redstone.client;

import com.cadmium.redstone.RedstoneOverhaulMod;
import com.cadmium.redstone.renderer.RendererPipeline;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side entry point for the Redstone Overhaul mod.
 * Initializes rendering systems and client-side features.
 */
@Environment(EnvType.CLIENT)
public class RedstoneOverhaulClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("redstone_overhaul/client");
    
    private static RendererPipeline rendererPipeline;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Redstone Overhaul Client");
        
        // Initialize the custom rendering pipeline
        rendererPipeline = new RendererPipeline();
        rendererPipeline.initialize();
        
        LOGGER.info("Redstone Overhaul Client initialized with GPU rendering pipeline");
    }
    
    public static RendererPipeline getRendererPipeline() {
        return rendererPipeline;
    }
}

