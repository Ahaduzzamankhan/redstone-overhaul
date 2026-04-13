package com.cadmium.redstone.node;

import net.minecraft.core.BlockPos;

/**
 * Represents a single redstone component/node in the signal graph.
 */
public class RedstoneNode {
    private final BlockPos position;
    private final NodeType type;
    private int signalStrength;
    private long lastUpdateTick;
    private boolean powered;
    
    public RedstoneNode(BlockPos position, NodeType type) {
        this.position = position;
        this.type = type;
        this.signalStrength = 0;
        this.lastUpdateTick = 0;
        this.powered = false;
    }
    
    public BlockPos getPosition() {
        return position;
    }
    
    public NodeType getType() {
        return type;
    }
    
    public int getSignalStrength() {
        return signalStrength;
    }
    
    public void setSignalStrength(int signalStrength) {
        this.signalStrength = Math.clamp(signalStrength, 0, 15);
        this.powered = signalStrength > 0;
    }
    
    public long getLastUpdateTick() {
        return lastUpdateTick;
    }
    
    public void setLastUpdateTick(long lastUpdateTick) {
        this.lastUpdateTick = lastUpdateTick;
    }
    
    public boolean isPowered() {
        return powered;
    }
    
    @Override
    public String toString() {
        return "RedstoneNode{pos=" + position + ", type=" + type + ", signal=" + signalStrength + "}";
    }
}

