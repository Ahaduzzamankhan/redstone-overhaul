package com.cadmium.redstone;

import com.cadmium.redstone.command.RedstoneCommand;
import com.cadmium.redstone.engine.RedstoneEngine;
import com.cadmium.redstone.network.RedstoneNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Redstone Overhaul mod.
 * Initializes the core redstone engine, commands, and networking.
 */
public class RedstoneOverhaulMod implements ModInitializer {
    public static final String MOD_ID = "redstone_overhaul";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static RedstoneEngine engine;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Redstone Overhaul Mod v{}", getModVersion());
        
        // Initialize the core redstone engine
        engine = new RedstoneEngine();
        
        // Register commands
        registerCommands();
        
        // Register networking handlers
        registerNetworking();
        
        // Register server lifecycle events
        registerServerEvents();
        
        LOGGER.info("Redstone Overhaul Mod initialized successfully");
    }
    
    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RedstoneCommand.register(dispatcher);
        });
        LOGGER.info("Registered /redstone command");
    }
    
    private void registerNetworking() {
        RedstoneNetworking.registerServerReceivers();
        LOGGER.info("Registered networking handlers");
    }
    
    private void registerServerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("Server starting - initializing RedstoneEngine for all worlds");
            engine.initialize(server);
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Server stopping - shutting down RedstoneEngine");
            if (engine != null) {
                engine.shutdown();
            }
        });
    }
    
    public static RedstoneEngine getEngine() {
        return engine;
    }
    
    public static String getModVersion() {
        return "0.0.1-alpha";
    }
}

