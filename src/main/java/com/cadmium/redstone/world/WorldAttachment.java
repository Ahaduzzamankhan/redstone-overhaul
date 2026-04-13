package com.cadmium.redstone.world;

import com.cadmium.redstone.ai.AICircuitController;
import com.cadmium.redstone.engine.RedstoneEngine;
import com.cadmium.redstone.engine.SignalGraph;
import com.cadmium.redstone.node.NodeType;
import com.cadmium.redstone.node.RedstoneNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * World integration layer that attaches redstone simulation to Minecraft worlds.
 * Manages signal graphs per chunk and handles world loading/unloading.
 */
public class WorldAttachment {
    private static final Logger LOGGER = LoggerFactory.getLogger("redstone_overhaul/world");
    
    // The server level this attachment is for
    private final ServerLevel level;
    
    // Reference to the redstone engine
    private final RedstoneEngine engine;
    
    // Signal graphs organized by chunk
    private final Map<ChunkPos, SignalGraph> chunkGraphs = new ConcurrentHashMap<>();
    
    // All signal graphs in this world
    private final List<SignalGraph> allGraphs = Collections.synchronizedList(new ArrayList<>());
    
    // Scheduled updates by position
    private final Map<BlockPos, Integer> scheduledUpdates = new ConcurrentHashMap<>();
    
    // AI controllers per graph
    private final Map<UUID, AICircuitController> aiControllers = new ConcurrentHashMap<>();
    
    // Lock for thread safety
    private final ReentrantReadWriteLock attachmentLock = new ReentrantReadWriteLock();
    
    // Current tick
    private long currentTick = 0;
    
    // Whether this world is loaded
    private volatile boolean loaded = false;
    
    /**
     * Create a new world attachment.
     */
    public WorldAttachment(ServerLevel level, RedstoneEngine engine) {
        this.level = level;
        this.engine = engine;
        this.loaded = true;
        
        LOGGER.info("WorldAttachment created for level {}", level.dimension().toString());
    }
    
    /**
     * Tick the world's redstone simulation.
     */
    public void tick() {
        if (!loaded) {
            return;
        }
        
        currentTick++;
        
        // Process scheduled updates
        processScheduledUpdates();
        
        // Tick all signal graphs
        for (SignalGraph graph : allGraphs) {
            if (graph.isDirty()) {
                graph.processUpdates(currentTick);
            }
        }
        
        // Run AI optimization periodically
        if (currentTick % 100 == 0) {
            runAIOptimization();
        }
    }
    
    /**
     * Process all scheduled updates.
     */
    private void processScheduledUpdates() {
        List<BlockPos> toProcess = new ArrayList<>();
        
        for (Map.Entry<BlockPos, Integer> entry : scheduledUpdates.entrySet()) {
            if (entry.getValue() <= 0) {
                toProcess.add(entry.getKey());
            } else {
                scheduledUpdates.put(entry.getKey(), entry.getValue() - 1);
            }
        }
        
        for (BlockPos pos : toProcess) {
            scheduledUpdates.remove(pos);
            processBlockUpdate(pos);
        }
    }
    
    /**
     * Process a single block update.
     */
    private void processBlockUpdate(BlockPos pos) {
        // Find the graph containing this position
        for (SignalGraph graph : allGraphs) {
            if (graph.getNodes().containsKey(pos)) {
                graph.scheduleSignalUpdate(pos, 0);
                break;
            }
        }
    }
    
    /**
     * Schedule an update for a position.
     */
    public void scheduleUpdate(BlockPos pos, int tickDelay) {
        scheduledUpdates.put(pos, tickDelay);
    }
    
    /**
     * Get signal strength at a position.
     */
    public int getSignalStrength(BlockPos pos) {
        for (SignalGraph graph : allGraphs) {
            int signal = graph.getCachedSignal(pos);
            if (signal > 0) {
                return signal;
            }
        }
        return 0;
    }
    
    /**
     * Set signal strength at a position.
     */
    public void setSignalStrength(BlockPos pos, int strength) {
        // Find or create graph for this position
        SignalGraph graph = getOrCreateGraphForPosition(pos);
        
        // Update node in graph
        if (graph.getNodes().containsKey(pos)) {
            RedstoneNode node = graph.getNodes().get(pos);
            node.setSignalStrength(strength);
        }
    }
    
    /**
     * Get or create a signal graph for a position.
     */
    private SignalGraph getOrCreateGraphForPosition(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        
        return chunkGraphs.computeIfAbsent(chunkPos, chunk -> {
            SignalGraph graph = new SignalGraph();
            allGraphs.add(graph);
            
            // Create AI controller for this graph
            AICircuitController controller = new AICircuitController(graph);
            aiControllers.put(graph.getId(), controller);
            
            LOGGER.debug("New SignalGraph created for chunk {} in level {}",
                chunk, level.dimension().toString());
            
            return graph;
        });
    }
    
    /**
     * Add a redstone component to the simulation.
     */
    public void addComponent(BlockPos pos, NodeType type) {
        SignalGraph graph = getOrCreateGraphForPosition(pos);
        graph.addNode(pos, type);
        
        // Connect to adjacent components
        connectToAdjacentComponents(pos);
    }
    
    /**
     * Remove a redstone component from the simulation.
     */
    public void removeComponent(BlockPos pos) {
        for (SignalGraph graph : allGraphs) {
            if (graph.getNodes().containsKey(pos)) {
                graph.removeNode(pos);
                
                // Remove graph if empty
                if (graph.getNodeCount() == 0) {
                    allGraphs.remove(graph);
                    aiControllers.remove(graph.getId());
                }
                break;
            }
        }
    }
    
    /**
     * Connect a position to adjacent components in the same chunk.
     */
    private void connectToAdjacentComponents(BlockPos pos) {
        SignalGraph graph = getOrCreateGraphForPosition(pos);
        
        BlockPos[] adjacent = {
            pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below()
        };
        
        for (BlockPos adjacentPos : adjacent) {
            if (graph.getNodes().containsKey(adjacentPos)) {
                graph.connectNodes(pos, adjacentPos);
            }
        }
    }
    
    /**
     * Run AI optimization for all graphs.
     */
    private void runAIOptimization() {
        for (AICircuitController controller : aiControllers.values()) {
            try {
                controller.applyOptimizations();
            } catch (Exception e) {
                LOGGER.warn("AI optimization failed for graph: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Get the AI controller for a graph.
     */
    public AICircuitController getAIController(UUID graphId) {
        return aiControllers.get(graphId);
    }
    
    /**
     * Get all signal graphs in this world.
     */
    public List<SignalGraph> getAllGraphs() {
        return Collections.unmodifiableList(allGraphs);
    }
    
    /**
     * Get total component count.
     */
    public int getTotalComponentCount() {
        return allGraphs.stream().mapToInt(SignalGraph::getNodeCount).sum();
    }
    
    /**
     * Check if the world is loaded.
     */
    public boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Unload the world attachment.
     */
    public void unload() {
        attachmentLock.writeLock().lock();
        try {
            loaded = false;
            allGraphs.clear();
            chunkGraphs.clear();
            aiControllers.clear();
            scheduledUpdates.clear();
            
            LOGGER.info("WorldAttachment unloaded for level {}", level.dimension().toString());
        } finally {
            attachmentLock.writeLock().unlock();
        }
    }
    
    /**
     * Get the current tick.
     */
    public long getCurrentTick() {
        return currentTick;
    }
    
    /**
     * Get the server level.
     */
    public ServerLevel getLevel() {
        return level;
    }
}




