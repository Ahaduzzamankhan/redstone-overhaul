package com.cadmium.redstone.ai;

import net.minecraft.core.BlockPos;

/**
 * Represents a signal routing decision made by the AI controller.
 */
public class SignalDecision {
    private final long timestamp;
    private final DecisionType type;
    private final BlockPos position;
    private final int signalStrength;
    private final String reason;
    
    public SignalDecision(DecisionType type, BlockPos position, int signalStrength, String reason) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.position = position;
        this.signalStrength = signalStrength;
        this.reason = reason;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public DecisionType getType() {
        return type;
    }
    
    public BlockPos getPosition() {
        return position;
    }
    
    public int getSignalStrength() {
        return signalStrength;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return String.format("SignalDecision{type=%s, pos=%s, signal=%d, reason='%s'}",
            type, position, signalStrength, reason);
    }
    
    /**
     * Types of signal routing decisions.
     */
    public enum DecisionType {
        ROUTE_SIGNAL,
        BOOST_SIGNAL,
        BLOCK_SIGNAL,
        REDIRECT_SIGNAL,
        OPTIMIZE_PATH
    }
}

