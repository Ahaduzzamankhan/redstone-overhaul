package com.cadmium.redstone.engine;

import com.cadmium.redstone.node.RedstoneNode;
import com.cadmium.redstone.node.NodeType;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Node-based signal network representing a connected redstone circuit.
 * Manages signal propagation, caching, and deterministic behavior.
 */
public class SignalGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger("redstone_overhaul/signalgraph");
    
    // Unique identifier for this graph
    private final UUID id = UUID.randomUUID();
    
    // All nodes in this graph
    private final Map<BlockPos, RedstoneNode> nodes = new ConcurrentHashMap<>();
    
    // Adjacency list for signal propagation
    private final Map<BlockPos, Set<BlockPos>> adjacencyList = new ConcurrentHashMap<>();
    
    // Signal cache to prevent recomputation
    private final Map<BlockPos, Integer> signalCache = new ConcurrentHashMap<>();
    
    // Nodes scheduled for update this tick
    private final PriorityQueue<ScheduledUpdate> scheduledUpdates = new PriorityQueue<>();
    
    // Lock for thread safety
    private final ReentrantReadWriteLock graphLock = new ReentrantReadWriteLock();
    
    // Whether this graph needs recomputation
    private volatile boolean dirty = false;
    
    // Last tick processed
    private long lastTickProcessed = 0;
    
    /**
     * Add a node to the signal graph.
     */
    public void addNode(BlockPos pos, NodeType type) {
        graphLock.writeLock().lock();
        try {
            RedstoneNode node = new RedstoneNode(pos, type);
            nodes.put(pos, node);
            adjacencyList.putIfAbsent(pos, ConcurrentHashMap.newKeySet());
            dirty = true;
        } finally {
            graphLock.writeLock().unlock();
        }
    }
    
    /**
     * Remove a node from the signal graph.
     */
    public void removeNode(BlockPos pos) {
        graphLock.writeLock().lock();
        try {
            nodes.remove(pos);
            adjacencyList.remove(pos);
            
            // Remove from all adjacency lists
            for (Set<BlockPos> neighbors : adjacencyList.values()) {
                neighbors.remove(pos);
            }
            
            signalCache.remove(pos);
            dirty = true;
        } finally {
            graphLock.writeLock().unlock();
        }
    }
    
    /**
     * Connect two nodes in the graph.
     */
    public void connectNodes(BlockPos from, BlockPos to) {
        graphLock.writeLock().lock();
        try {
            adjacencyList.computeIfAbsent(from, k -> ConcurrentHashMap.newKeySet()).add(to);
            adjacencyList.computeIfAbsent(to, k -> ConcurrentHashMap.newKeySet()).add(from);
            dirty = true;
        } finally {
            graphLock.writeLock().unlock();
        }
    }
    
    /**
     * Disconnect two nodes in the graph.
     */
    public void disconnectNodes(BlockPos from, BlockPos to) {
        graphLock.writeLock().lock();
        try {
            if (adjacencyList.containsKey(from)) {
                adjacencyList.get(from).remove(to);
            }
            if (adjacencyList.containsKey(to)) {
                adjacencyList.get(to).remove(from);
            }
            dirty = true;
        } finally {
            graphLock.writeLock().unlock();
        }
    }
    
    /**
     * Schedule a signal update for propagation.
     */
    public void scheduleSignalUpdate(BlockPos pos, int delay) {
        graphLock.writeLock().lock();
        try {
            scheduledUpdates.add(new ScheduledUpdate(pos, delay));
        } finally {
            graphLock.writeLock().unlock();
        }
    }
    
    /**
     * Process all scheduled updates for the current tick.
     */
    public void processUpdates(long currentTick) {
        graphLock.writeLock().lock();
        try {
            lastTickProcessed = currentTick;
            
            // Process all updates with delay <= 0
            while (!scheduledUpdates.isEmpty() && scheduledUpdates.peek().delay <= 0) {
                ScheduledUpdate update = scheduledUpdates.poll();
                processNodeUpdate(update.pos);
                
                // Decrement delays for remaining updates
                for (ScheduledUpdate remaining : scheduledUpdates) {
                    remaining.delay--;
                }
            }
            
            dirty = false;
        } finally {
            graphLock.writeLock().unlock();
        }
    }
    
    /**
     * Process an update for a specific node.
     */
    private void processNodeUpdate(BlockPos pos) {
        RedstoneNode node = nodes.get(pos);
        if (node == null) {
            return;
        }
        
        // Calculate new signal strength based on node type and neighbors
        int newSignal = calculateSignalStrength(pos);
        
        // Update cache
        int oldSignal = signalCache.getOrDefault(pos, 0);
        if (newSignal != oldSignal) {
            signalCache.put(pos, newSignal);
            node.setSignalStrength(newSignal);
            
            // Propagate to neighbors
            Set<BlockPos> neighbors = adjacencyList.get(pos);
            if (neighbors != null) {
                for (BlockPos neighbor : neighbors) {
                    scheduleSignalUpdate(neighbor, 1);
                }
            }
        }
    }
    
    /**
     * Calculate signal strength for a node based on its type and inputs.
     */
    private int calculateSignalStrength(BlockPos pos) {
        RedstoneNode node = nodes.get(pos);
        if (node == null) {
            return 0;
        }
        
        return switch (node.getType()) {
            case WIRE -> calculateWireSignal(pos);
            case AND_GATE -> calculateAndGateSignal(pos);
            case OR_GATE -> calculateOrGateSignal(pos);
            case XOR_GATE -> calculateXorGateSignal(pos);
            case NAND_GATE -> calculateNandGateSignal(pos);
            case COMPARATOR -> calculateComparatorSignal(pos);
            case MEMORY_CELL -> calculateMemoryCellSignal(pos);
            case REPEATER -> calculateRepeaterSignal(pos);
            default -> 0;
        };
    }
    
    private int calculateWireSignal(BlockPos pos) {
        int maxSignal = 0;
        Set<BlockPos> neighbors = adjacencyList.get(pos);
        if (neighbors != null) {
            for (BlockPos neighbor : neighbors) {
                int neighborSignal = signalCache.getOrDefault(neighbor, 0);
                if (neighborSignal > 0) {
                    maxSignal = Math.max(maxSignal, neighborSignal - 1);
                }
            }
        }
        return maxSignal;
    }
    
    private int calculateAndGateSignal(BlockPos pos) {
        int minInput = 15;
        Set<BlockPos> neighbors = adjacencyList.get(pos);
        if (neighbors == null || neighbors.isEmpty()) {
            return 0;
        }
        for (BlockPos neighbor : neighbors) {
            minInput = Math.min(minInput, signalCache.getOrDefault(neighbor, 0));
        }
        return minInput;
    }
    
    private int calculateOrGateSignal(BlockPos pos) {
        int maxInput = 0;
        Set<BlockPos> neighbors = adjacencyList.get(pos);
        if (neighbors != null) {
            for (BlockPos neighbor : neighbors) {
                maxInput = Math.max(maxInput, signalCache.getOrDefault(neighbor, 0));
            }
        }
        return maxInput;
    }
    
    private int calculateXorGateSignal(BlockPos pos) {
        int activeInputs = 0;
        Set<BlockPos> neighbors = adjacencyList.get(pos);
        if (neighbors != null) {
            for (BlockPos neighbor : neighbors) {
                if (signalCache.getOrDefault(neighbor, 0) > 0) {
                    activeInputs++;
                }
            }
        }
        return (activeInputs % 2 == 1) ? 15 : 0;
    }
    
    private int calculateNandGateSignal(BlockPos pos) {
        int andResult = calculateAndGateSignal(pos);
        return andResult > 0 ? 0 : 15;
    }
    
    private int calculateComparatorSignal(BlockPos pos) {
        // Simplified comparator logic
        Set<BlockPos> neighbors = adjacencyList.get(pos);
        if (neighbors == null || neighbors.isEmpty()) {
            return 0;
        }
        return signalCache.getOrDefault(neighbors.iterator().next(), 0);
    }
    
    private int calculateMemoryCellSignal(BlockPos pos) {
        // Memory cell retains its state
        return signalCache.getOrDefault(pos, 0);
    }
    
    private int calculateRepeaterSignal(BlockPos pos) {
        Set<BlockPos> neighbors = adjacencyList.get(pos);
        if (neighbors != null) {
            for (BlockPos neighbor : neighbors) {
                int inputSignal = signalCache.getOrDefault(neighbor, 0);
                if (inputSignal > 0) {
                    return 15;
                }
            }
        }
        return 0;
    }
    
    /**
     * Get the cached signal strength at a position.
     */
    public int getCachedSignal(BlockPos pos) {
        graphLock.readLock().lock();
        try {
            return signalCache.getOrDefault(pos, 0);
        } finally {
            graphLock.readLock().unlock();
        }
    }
    
    /**
     * Get all nodes in this graph.
     */
    public Map<BlockPos, RedstoneNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }
    
    /**
     * Get the graph ID.
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Check if the graph is dirty and needs recomputation.
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * Get the number of nodes in this graph.
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * Clear the signal cache.
     */
    public void clearCache() {
        graphLock.writeLock().lock();
        try {
            signalCache.clear();
            dirty = true;
        } finally {
            graphLock.writeLock().unlock();
        }
    }
    
    /**
     * Scheduled update entry for signal propagation.
     */
    private static class ScheduledUpdate implements Comparable<ScheduledUpdate> {
        final BlockPos pos;
        int delay;
        
        ScheduledUpdate(BlockPos pos, int delay) {
            this.pos = pos;
            this.delay = delay;
        }
        
        @Override
        public int compareTo(ScheduledUpdate other) {
            return Integer.compare(this.delay, other.delay);
        }
    }
}

