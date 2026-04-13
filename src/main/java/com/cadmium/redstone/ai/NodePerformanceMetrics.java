package com.cadmium.redstone.ai;

import com.cadmium.redstone.node.NodeType;
import com.cadmium.redstone.node.RedstoneNode;
import net.minecraft.core.BlockPos;

/**
 * Performance metrics for a single redstone node.
 */
public class NodePerformanceMetrics {
    private final BlockPos position;
    private NodeType nodeType;
    private int signalStrength;
    private int updateCount;
    private double averageDelay;
    private int neighborCount;
    private boolean active;
    private long lastActiveTick;
    private long totalActiveTicks;
    
    public NodePerformanceMetrics() {
        this.position = new BlockPos(0, 0, 0);
        this.nodeType = NodeType.WIRE;
    }
    
    public void update(RedstoneNode node) {
        this.nodeType = node.getType();
        this.signalStrength = node.getSignalStrength();
        this.active = node.isPowered();
        
        if (active) {
            totalActiveTicks++;
        }
        lastActiveTick = System.currentTimeMillis();
    }
    
    public void incrementUpdateCount() {
        this.updateCount++;
    }
    
    public void setAverageDelay(double delay) {
        this.averageDelay = delay;
    }
    
    public void setNeighborCount(int count) {
        this.neighborCount = count;
    }
    
    // Getters
    public BlockPos getPosition() {
        return position;
    }
    
    public NodeType getNodeType() {
        return nodeType;
    }
    
    public int getSignalStrength() {
        return signalStrength;
    }
    
    public int getUpdateCount() {
        return updateCount;
    }
    
    public double getAverageDelay() {
        return averageDelay;
    }
    
    public int getNeighborCount() {
        return neighborCount;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public long getLastActiveTick() {
        return lastActiveTick;
    }
    
    public long getTotalActiveTicks() {
        return totalActiveTicks;
    }
    
    @Override
    public String toString() {
        return String.format("NodeMetrics{pos=%s, type=%s, signal=%d, updates=%d, delay=%.2f}",
            position, nodeType, signalStrength, updateCount, averageDelay);
    }
}

