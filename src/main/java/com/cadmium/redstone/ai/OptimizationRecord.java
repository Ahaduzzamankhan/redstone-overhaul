package com.cadmium.redstone.ai;

/**
 * Record of an optimization that was applied by the AI controller.
 */
public class OptimizationRecord {
    private final long timestamp;
    private final CircuitAnalysis analysis;
    private final java.util.function.Predicate<OptimizationSuggestion> appliedSuggestions;
    
    public OptimizationRecord(long timestamp, CircuitAnalysis analysis, 
                              java.util.function.Predicate<OptimizationSuggestion> appliedSuggestions) {
        this.timestamp = timestamp;
        this.analysis = analysis;
        this.appliedSuggestions = appliedSuggestions;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public CircuitAnalysis getAnalysis() {
        return analysis;
    }
    
    public java.util.function.Predicate<OptimizationSuggestion> getAppliedSuggestions() {
        return appliedSuggestions;
    }
}

