package com.cadmium.redstone.engine;

import com.cadmium.redstone.RedstoneOverhaulMod;
import com.cadmium.redstone.world.WorldAttachment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Core redstone simulation engine.
 * Manages tick-based circuit simulation with deterministic signal propagation.
 * Uses multi-threaded chunk-level parallel processing for performance.
 */
public class RedstoneEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger("redstone_overhaul/engine");
    
    // Maximum components per signal graph before splitting
    private static final int MAX_GRAPH_SIZE = 10000;
    
    // Server instance
    private MinecraftServer server;
    
    // World attachments for each world (keyed by dimension string)
    private final Map<String, WorldAttachment> worldAttachments = new ConcurrentHashMap<>();
    
    // Thread pool for parallel simulation
    private ExecutorService simulationExecutor;
    
    // Read-write lock for thread safety
    private final ReentrantReadWriteLock engineLock = new ReentrantReadWriteLock();
    
    // Whether the engine is running
    private volatile boolean running = false;
    
    // Performance statistics
    private final EngineStatistics statistics = new EngineStatistics();
    
    /**
     * Initialize the engine with the server instance.
     */
    public void initialize(MinecraftServer server) {
        engineLock.writeLock().lock();
        try {
            this.server = server;
            this.running = true;
            
            // Create thread pool with cores matching available processors
            int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
            this.simulationExecutor = Executors.newFixedThreadPool(threadCount, runnable -> {
                Thread thread = new Thread(runnable, "RedstoneEngine-Worker");
                thread.setDaemon(true);
                return thread;
            });
            
            LOGGER.info("RedstoneEngine initialized with {} simulation threads", threadCount);
        } finally {
            engineLock.writeLock().unlock();
        }
    }
    
    /**
     * Shutdown the engine gracefully.
     */
    public void shutdown() {
        engineLock.writeLock().lock();
        try {
            this.running = false;
            
            if (simulationExecutor != null) {
                simulationExecutor.shutdown();
                try {
                    if (!simulationExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        simulationExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    simulationExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            
            worldAttachments.clear();
            LOGGER.info("RedstoneEngine shut down");
        } finally {
            engineLock.writeLock().unlock();
        }
    }
    
    /**
     * Get or create a world attachment for the given world.
     */
    public WorldAttachment getWorldAttachment(Level level) {
        engineLock.readLock().lock();
        try {
            return worldAttachments.computeIfAbsent(level.dimension().toString(), 
                key -> new WorldAttachment((ServerLevel) level, this));
        } finally {
            engineLock.readLock().unlock();
        }
    }
    
    /**
     * Schedule a component update for the next tick.
     */
    public void scheduleUpdate(Level world, BlockPos pos, int tickDelay) {
        WorldAttachment attachment = getWorldAttachment(world);
        attachment.scheduleUpdate(pos, tickDelay);
    }
    
    /**
     * Process a single tick for all active signal graphs.
     */
    public void tick() {
        if (!running) {
            return;
        }
        
        engineLock.readLock().lock();
        try {
            long startTime = System.nanoTime();
            
            // Tick all world attachments
            for (WorldAttachment attachment : worldAttachments.values()) {
                if (attachment.isLoaded()) {
                    attachment.tick();
                }
            }
            
            long endTime = System.nanoTime();
            statistics.lastTickTime = (endTime - startTime) / 1_000_000.0;
            statistics.totalTicks++;
            
        } finally {
            engineLock.readLock().unlock();
        }
    }
    
    /**
     * Get the current signal strength at a position.
     */
    public int getSignalStrength(Level world, BlockPos pos) {
        WorldAttachment attachment = getWorldAttachment(world);
        return attachment.getSignalStrength(pos);
    }
    
    /**
     * Set the signal strength at a position.
     */
    public void setSignalStrength(Level world, BlockPos pos, int strength) {
        WorldAttachment attachment = getWorldAttachment(world);
        attachment.setSignalStrength(pos, strength);
    }
    
    /**
     * Get the simulation executor for parallel tasks.
     */
    public ExecutorService getSimulationExecutor() {
        return simulationExecutor;
    }
    
    /**
     * Check if the engine is running.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Get performance statistics.
     */
    public EngineStatistics getStatistics() {
        return statistics;
    }
    
    /**
     * Performance statistics for the engine.
     */
    public static class EngineStatistics {
        public long totalTicks = 0;
        public double lastTickTime = 0.0;
        public double averageTickTime = 0.0;
        public int activeGraphs = 0;
        public int totalComponents = 0;
        public int cachedSignals = 0;
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("totalTicks", totalTicks);
            map.put("lastTickTime", String.format("%.2fms", lastTickTime));
            map.put("activeGraphs", activeGraphs);
            map.put("totalComponents", totalComponents);
            map.put("cachedSignals", cachedSignals);
            return map;
        }
    }
}



