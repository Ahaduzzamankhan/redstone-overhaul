package com.cadmium.redstone.command;

import com.cadmium.redstone.RedstoneOverhaulMod;
import com.cadmium.redstone.ai.AICircuitController;
import com.cadmium.redstone.ai.CircuitAnalysis;
import com.cadmium.redstone.engine.RedstoneEngine;
import com.cadmium.redstone.engine.SignalGraph;
import com.cadmium.redstone.world.WorldAttachment;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Map;

/**
 * Main command class for /redstone command.
 * Provides subcommands: debug, profile, status, optimize
 */
public class RedstoneCommand {

    /**
     * Debug rendering modes (local copy since RendererPipeline is client-only).
     */
    public enum DebugMode {
        OFF, SIGNAL_PATHS, NODE_STATES, HEATMAP, PERFORMANCE
    }

    /**
     * Register the /redstone command and its subcommands.
     */
    @SuppressWarnings("unchecked")
    public static void register(CommandDispatcher<?> dispatcher) {
        CommandDispatcher<CommandSourceStack> cmdDispatcher = (CommandDispatcher<CommandSourceStack>) dispatcher;
        cmdDispatcher.register(
            Commands.literal("redstone")
                .requires(source -> true) // TODO: Fix permissions for MC 26.1
                .then(Commands.literal("debug")
                    .then(Commands.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("off");
                            builder.suggest("paths");
                            builder.suggest("nodes");
                            builder.suggest("heatmap");
                            builder.suggest("performance");
                            return builder.buildFuture();
                        })
                        .executes(RedstoneCommand::setDebugMode)
                    )
                )
                .then(Commands.literal("profile")
                    .executes(RedstoneCommand::showProfile)
                )
                .then(Commands.literal("status")
                    .executes(RedstoneCommand::showStatus)
                )
                .then(Commands.literal("optimize")
                    .executes(RedstoneCommand::runOptimization)
                )
                .then(Commands.literal("learning")
                    .then(Commands.argument("enabled", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("true");
                            builder.suggest("false");
                            return builder.buildFuture();
                        })
                        .executes(RedstoneCommand::toggleLearning)
                    )
                )
        );
    }

    /**
     * Execute /redstone debug <mode>
     */
    private static int setDebugMode(CommandContext<CommandSourceStack> context) {
        String mode = StringArgumentType.getString(context, "mode");

        DebugMode debugMode = switch (mode.toLowerCase()) {
            case "off" -> DebugMode.OFF;
            case "paths" -> DebugMode.SIGNAL_PATHS;
            case "nodes" -> DebugMode.NODE_STATES;
            case "heatmap" -> DebugMode.HEATMAP;
            case "performance" -> DebugMode.PERFORMANCE;
            default -> DebugMode.OFF;
        };

        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Debug mode set to: " + debugMode).withStyle(ChatFormatting.GREEN), false);

        return 1;
    }

    /**
     * Execute /redstone profile
     */
    private static int showProfile(CommandContext<CommandSourceStack> context) {
        RedstoneEngine engine = RedstoneOverhaulMod.getEngine();
        if (engine == null) {
            context.getSource().sendSuccess(() -> Component.literal("Engine not initialized").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        RedstoneEngine.EngineStatistics stats = engine.getStatistics();
        Map<String, Object> statsMap = stats.toMap();

        context.getSource().sendSuccess(() -> Component.literal("=== Redstone Engine Profile ===").withStyle(ChatFormatting.GOLD), false);
        context.getSource().sendSuccess(() -> Component.literal("Total Ticks: " + statsMap.get("totalTicks")).withStyle(ChatFormatting.YELLOW), false);
        context.getSource().sendSuccess(() -> Component.literal("Last Tick Time: " + statsMap.get("lastTickTime")).withStyle(ChatFormatting.YELLOW), false);
        context.getSource().sendSuccess(() -> Component.literal("Active Graphs: " + statsMap.get("activeGraphs")).withStyle(ChatFormatting.YELLOW), false);
        context.getSource().sendSuccess(() -> Component.literal("Total Components: " + statsMap.get("totalComponents")).withStyle(ChatFormatting.YELLOW), false);
        context.getSource().sendSuccess(() -> Component.literal("Cached Signals: " + statsMap.get("cachedSignals")).withStyle(ChatFormatting.YELLOW), false);

        return 1;
    }

    /**
     * Execute /redstone status
     */
    private static int showStatus(CommandContext<CommandSourceStack> context) {
        RedstoneEngine engine = RedstoneOverhaulMod.getEngine();
        if (engine == null) {
            context.getSource().sendSuccess(() -> Component.literal("Engine not initialized").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal("=== Redstone Engine Status ===").withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.literal("Running: " + (engine.isRunning() ? "Yes" : "No")).withStyle(ChatFormatting.GREEN), false);

        try {
            var server = source.getServer();
            for (var level : server.getAllLevels()) {
                WorldAttachment attachment = engine.getWorldAttachment(level);
                if (attachment != null && attachment.isLoaded()) {
                    int components = attachment.getTotalComponentCount();
                    int graphs = attachment.getAllGraphs().size();
                    source.sendSuccess(() -> Component.literal("Level " + level.dimension().toString() +
                        ": " + components + " components, " + graphs + " graphs").withStyle(ChatFormatting.WHITE), false);
                }
            }
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("Error getting level info: " + e.getMessage()).withStyle(ChatFormatting.RED), false);
        }

        return 1;
    }

    /**
     * Execute /redstone optimize
     */
    private static int runOptimization(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Running AI optimization...").withStyle(ChatFormatting.GREEN), false);

        try {
            var server = source.getServer();
            RedstoneEngine engine = RedstoneOverhaulMod.getEngine();

            if (engine != null) {
                for (var level : server.getAllLevels()) {
                    WorldAttachment attachment = engine.getWorldAttachment(level);
                    if (attachment != null && attachment.isLoaded()) {
                        for (SignalGraph graph : attachment.getAllGraphs()) {
                            AICircuitController controller = attachment.getAIController(graph.getId());
                            if (controller != null) {
                                CircuitAnalysis analysis = controller.analyzeCircuit();

                                source.sendSuccess(() -> Component.literal("Level " + level.dimension().toString() +
                                    " - Graph " + graph.getId() + ": " +
                                    analysis.getTotalNodes() + " nodes, " +
                                    analysis.getSuggestions().size() + " suggestions").withStyle(ChatFormatting.WHITE), false);
                            }
                        }
                    }
                }
            }

            source.sendSuccess(() -> Component.literal("Optimization complete").withStyle(ChatFormatting.GREEN), false);
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("Optimization failed: " + e.getMessage()).withStyle(ChatFormatting.RED), false);
            return 0;
        }

        return 1;
    }

    /**
     * Execute /redstone learning <true/false>
     */
    private static int toggleLearning(CommandContext<CommandSourceStack> context) {
        String enabled = StringArgumentType.getString(context, "enabled");
        boolean learningEnabled = Boolean.parseBoolean(enabled);

        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Learning mode " + (learningEnabled ? "enabled" : "disabled")).withStyle(ChatFormatting.GREEN), false);

        return 1;
    }
}

