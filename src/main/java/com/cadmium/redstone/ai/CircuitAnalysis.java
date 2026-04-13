package com.cadmium.redstone.ai;

import com.cadmium.redstone.node.NodeType;
import com.cadmium.redstone.node.RedstoneNode;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Circuit analysis result containing performance metrics and optimization suggestions.
 */
public class CircuitAnalysis {
    private final Map<BlockPos, NodePerformanceMetrics> nodeMetrics = new HashMap<>();
    private final List<BlockPos> bottlenecks = new ArrayList<>();
    private final List<OptimizationSuggestion> suggestions = new ArrayList<>();
    
    private double averageSignalDelay = 0.0;
    private double maxSignalDelay = 0.0;
    private int totalNodes = 0;
    private int activeNodes = 0;
    private double efficiency = 0.0;
    
    /**
     * Add a node's metrics to the analysis.
     */
    public void addNode(NodePerformanceMetrics metrics) {
        nodeMetrics.put(metrics.getPosition(), metrics);
        totalNodes++;
        if (metrics.isActive()) {
            activeNodes++;
        }
    }
    
    /**
     * Find bottleneck nodes with highest signal delays.
     */
    public void findBottlenecks() {
        // Sort nodes by delay
        List<NodePerformanceMetrics> sortedNodes = new ArrayList<>(nodeMetrics.values());
        sortedNodes.sort((a, b) -> Double.compare(b.getAverageDelay(), a.getAverageDelay()));
        
        // Take top 10% as bottlenecks
        int bottleneckCount = Math.max(1, totalNodes / 10);
        for (int i = 0; i < Math.min(bottleneckCount, sortedNodes.size()); i++) {
            bottlenecks.add(sortedNodes.get(i).getPosition());
        }
        
        // Calculate statistics
        if (!sortedNodes.isEmpty()) {
            maxSignalDelay = sortedNodes.getFirst().getAverageDelay();
            averageSignalDelay = sortedNodes.stream()
                .mapToDouble(NodePerformanceMetrics::getAverageDelay)
                .average()
                .orElse(0.0);
        }
        
        // Calculate efficiency
        efficiency = totalNodes > 0 ? (double) activeNodes / totalNodes * 100.0 : 0.0;
    }
    
    /**
     * Generate optimization suggestions based on analysis.
     */
    public void generateSuggestions() {
        for (Map.Entry<BlockPos, NodePerformanceMetrics> entry : nodeMetrics.entrySet()) {
            BlockPos pos = entry.getKey();
            NodePerformanceMetrics metrics = entry.getValue();
            
            // Suggest adding repeaters for weak signals
            if (metrics.getSignalStrength() < 5 && metrics.getUpdateCount() > 10) {
                suggestions.add(new OptimizationSuggestion(
                    OptimizationSuggestion.OptimizationType.ADD_REPEATER,
                    pos,
                    "Signal strength is low (" + metrics.getSignalStrength() + "). Add a repeater to boost signal."
                ));
            }

            // Suggest removing redundant wires
            if (metrics.getNeighborCount() == 2 && metrics.getUpdateCount() == 0) {
                suggestions.add(new OptimizationSuggestion(
                    OptimizationSuggestion.OptimizationType.REMOVE_REDUNDANT_WIRE,
                    pos,
                    "Wire appears redundant with no signal changes. Consider removing."
                ));
            }

            // Suggest rerouting for high-traffic nodes
            if (metrics.getUpdateCount() > 100) {
                suggestions.add(new OptimizationSuggestion(
                    OptimizationSuggestion.OptimizationType.REROUTE_SIGNAL,
                    pos,
                    "High traffic node (" + metrics.getUpdateCount() + " updates). Consider rerouting."
                ));
            }

            // Suggest consolidating gates
            if (metrics.getNodeType().isLogicGate() && metrics.getNeighborCount() > 4) {
                suggestions.add(new OptimizationSuggestion(
                    OptimizationSuggestion.OptimizationType.CONSOLIDATE_GATES,
                    pos,
                    "Complex gate with many connections. Consider simplifying."
                ));
            }
        }
    }
    
    // Getters
    public Map<BlockPos, NodePerformanceMetrics> getNodeMetrics() {
        return nodeMetrics;
    }
    
    public List<BlockPos> getBottlenecks() {
        return bottlenecks;
    }
    
    public List<OptimizationSuggestion> getSuggestions() {
        return suggestions;
    }
    
    public double getAverageSignalDelay() {
        return averageSignalDelay;
    }
    
    public double getMaxSignalDelay() {
        return maxSignalDelay;
    }
    
    public int getTotalNodes() {
        return totalNodes;
    }
    
    public int getActiveNodes() {
        return activeNodes;
    }
    
    public double getEfficiency() {
        return efficiency;
    }
    
    @Override
    public String toString() {
        return String.format("CircuitAnalysis{nodes=%d, active=%d, avg_delay=%.2f, efficiency=%.1f%%}",
            totalNodes, activeNodes, averageSignalDelay, efficiency);
    }
}

