# Redstone Overhaul Mod - Documentation

## Overview
The Redstone Overhaul Mod is a comprehensive redesign of Minecraft's redstone system for Fabric (Minecraft 1.26.1). It transforms redstone into a modern, high-performance, programmable logic system with AI-powered optimization and GPU-accelerated rendering.

---

## 📁 Project Structure

```
redstone-overhaul-mod/
├── src/
│   ├── main/
│   │   ├── java/com/cadmium/redstone/
│   │   │   ├── RedstoneOverhaulMod.java        # Main mod entry point
│   │   │   ├── ai/                              # AI circuit controller
│   │   │   │   ├── AICircuitController.java
│   │   │   │   ├── CircuitAnalysis.java
│   │   │   │   ├── NodePerformanceMetrics.java
│   │   │   │   ├── OptimizationSuggestion.java
│   │   │   │   ├── OptimizationRecord.java
│   │   │   │   └── SignalDecision.java
│   │   │   ├── command/                         # Debug commands
│   │   │   │   └── RedstoneCommand.java
│   │   │   ├── engine/                          # Core simulation
│   │   │   │   ├── RedstoneEngine.java
│   │   │   │   └── SignalGraph.java
│   │   │   ├── mixin/                           # Vanilla overrides
│   │   │   │   ├── RedstoneWireBlockMixin.java
│   │   │   │   ├── RedstoneWireBlockEntityMixin.java
│   │   │   │   ├── WorldTickManagerMixin.java
│   │   │   │   └── ServerWorldMixin.java
│   │   │   ├── network/                         # Multiplayer sync
│   │   │   │   └── RedstoneNetworking.java
│   │   │   ├── node/                            # Node types
│   │   │   │   ├── RedstoneNode.java
│   │   │   │   └── NodeType.java
│   │   │   └── world/                           # World integration
│   │   │       └── WorldAttachment.java
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       └── redstone_overhaul.mixins.json
│   └── client/
│       ├── java/com/cadmium/redstone/
│       │   ├── client/
│       │   │   └── RedstoneOverhaulClient.java
│       │   ├── mixin/
│       │   │   ├── RedstoneWireBlockRendererMixin.java
│       │   │   └── WorldRendererMixin.java
│       │   └── renderer/
│       │       └── RendererPipeline.java
├── build.gradle
├── gradle.properties
└── settings.gradle
```

---

## 🏗️ Architecture

### Core Systems

#### 1. RedstoneEngine
The central simulation engine managing all redstone logic across worlds.
- **Multi-threaded**: Uses thread pool for parallel simulation
- **Deterministic**: No randomness in signal propagation
- **Tick-based**: Processes updates on a scheduled tick basis
- **Chunk-aware**: Organizes simulation by chunks for performance

#### 2. SignalGraph
Node-based signal network representing connected redstone circuits.
- **Signal Caching**: Prevents unnecessary recomputation
- **Deterministic Propagation**: Signals propagate predictably
- **Dynamic Connectivity**: Automatically connects/disconnects nodes
- **Supports 30+ Component Types**: Wire, gates, memory cells, I/O devices

#### 3. AICircuitController
AI-powered optimization layer for intelligent signal routing.
- **A* Pathfinding**: Finds optimal signal paths
- **Performance Analysis**: Identifies bottlenecks
- **Adaptive Optimization**: Suggests and applies improvements
- **Learning Mode**: Remembers successful optimizations

#### 4. RendererPipeline
GPU-accelerated rendering system for redstone visualization.
- **Instanced Rendering**: High-performance GPU rendering
- **Animated Signals**: Real-time signal flow visualization
- **Debug Overlays**: Multiple visualization modes
- **Color-Coded Strength**: Signal strength displayed via color

#### 5. WorldAttachment
Integration layer between the mod and Minecraft worlds.
- **Per-World Management**: Each world has its own attachment
- **Chunk Organization**: Graphs organized by chunk position
- **Automatic Lifecycle**: Handles world load/unload

---

## 🎮 Commands

All commands require operator permission (level 2+).

### `/redstone debug <mode>`
Sets the debug visualization mode.

**Modes:**
- `off` - Disable debug rendering
- `paths` - Show signal paths between components
- `nodes` - Show node states with colored overlays
- `heatmap` - Show circuit activity heatmap
- `performance` - Show performance metrics overlay

**Example:**
```
/redstone debug paths
```

### `/redstone profile`
Displays detailed performance statistics for the redstone engine.

**Output includes:**
- Total ticks processed
- Last tick time (ms)
- Active signal graphs
- Total components simulated
- Cached signal count

**Example:**
```
/redstone profile
```

### `/redstone status`
Shows current engine status and world information.

**Output includes:**
- Engine running state
- Per-world component and graph counts

**Example:**
```
/redstone status
```

### `/redstone optimize`
Runs AI optimization on all active circuits.

**Output includes:**
- Analysis results per world/graph
- Optimization suggestions

**Example:**
```
/redstone optimize
```

### `/redstone learning <true/false>`
Enables or disables AI learning mode.

**Example:**
```
/redstone learning true
```

---

## 🔌 Supported Components

### Basic Components
- **Redstone Wire** - Signal transmission
- **Redstone Repeater** - Signal boost and delay
- **Redstone Comparator** - Signal comparison
- **Redstone Torch** - Signal inversion

### Logic Gates
- **AND Gate** - Output = A AND B
- **OR Gate** - Output = A OR B
- **XOR Gate** - Output = A XOR B
- **NAND Gate** - Output = NOT (A AND B)
- **NOR Gate** - Output = NOT (A OR B)
- **NOT Gate** - Output = NOT A
- **XNOR Gate** - Output = NOT (A XOR B)

### Memory Elements
- **Memory Cell (RS Latch)** - Basic 1-bit storage
- **T Flip-Flop** - Toggle flip-flop
- **D Flip-Flop** - Data flip-flop
- **JK Flip-Flop** - Versatile flip-flop

### Advanced Components
- **Counter** - Count signal pulses
- **Timer** - Timed signal generation
- **Multiplexer** - Signal selector
- **Demultiplexer** - Signal distributor
- **Encoder** - Encode signals
- **Decoder** - Decode signals

### I/O Devices
- **Lever** - Manual input
- **Button** - Temporary input
- **Pressure Plate** - Entity detection
- **Tripwire** - Entity detection
- **Observer** - Block change detection
- **Piston** - Block pushing
- **Sticky Piston** - Block pulling/pushing
- **Redstone Lamp** - Light output
- **Dropper** - Item output
- **Dispenser** - Item output
- **Hopper** - Item transfer
- **Note Block** - Sound output
- **Door/Trapdoor/Fence Gate** - Access control
- **Powered Rail** - Rail activation

---

## ⚡ Performance Features

### Multi-Threading
- Thread pool sized to half of available CPU cores
- Per-chunk parallel processing
- Lock-free data structures where possible

### Signal Caching
- Prevents recomputation of unchanged signals
- Automatic cache invalidation on changes
- O(1) cache lookups

### Chunk-Level Parallelism
- Each chunk processes independently
- No cross-chunk synchronization overhead
- Efficient for large-scale builds

### A* Pathfinding Optimization
- Finds shortest signal paths
- Avoids unnecessary computations
- Cached paths for repeated use

---

## 🎨 Rendering Features

### GPU Instancing
- Thousands of components rendered efficiently
- Single draw call for all instances
- Real-time signal strength colors

### Debug Visualization Modes

#### Signal Paths
Shows active signal flow between components with colored lines.

#### Node States
Displays each node's current state with color-coded overlays.

#### Heatmap
Visual representation of circuit activity - hotter colors = more active.

#### Performance
Real-time FPS and tick time statistics overlay.

---

## 🌐 Multiplayer Support

### Server-Client Synchronization
- Signal state updates sent to tracking players
- Bulk sync on player join
- Debug data sent on request

### Network Packets
- `signal_update` - Signal strength changes
- `circuit_info` - Circuit structure information
- `debug_data` - Debug visualization data

---

## 🧪 Example Circuits

### 1. Simple AND Gate
```
Lever ──┐
        ├── AND Gate ── Redstone Lamp
Lever ──┘
```
**Behavior:** Lamp lights only when both levers are ON.

### 2. RS Latch Memory Cell
```
Lever (Set) ──┐
              ├── RS Latch ── Redstone Lamp
Lever (Reset)─┘
```
**Behavior:** Lamp stays ON after Set lever is activated, turns OFF after Reset lever is activated.

### 3. Clock Generator
```
Repeater ───┐
            ├── Timer ── Redstone Lamp
            └───────────┘
```
**Behavior:** Lamp blinks on/off automatically at timed intervals.

### 4. 4-Bit Counter
```
Button ── Counter(4-bit) ── 4x Redstone Lamps
```
**Behavior:** Each button press increments the counter, displayed on 4 lamps (binary).

### 5. Multiplexer Circuit
```
Input A ──┐
          ├── Multiplexer ── Output
Input B ──┘
Selector ──┘
```
**Behavior:** Output shows Input A or B based on Selector signal.

---

## 🔧 Development Guide

### Building the Mod
```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

### Running in Development
```bash
# Run client
./gradlew runClient

# Run server
./gradlew runServer
```

### Adding New Component Types
1. Add enum value to `NodeType.java`
2. Implement signal calculation in `SignalGraph.calculateSignalStrength()`
3. Add mixin if needed for vanilla component override

### Adding New Debug Modes
1. Add enum value to `RendererPipeline.DebugMode`
2. Implement render method in `RendererPipeline`
3. Add command suggestion in `RedstoneCommand`

---

## ⚠️ Known Limitations

1. **Vanilla Compatibility**: Worlds with this mod will have modified redstone behavior. Disable mod to restore vanilla behavior.

2. **Rendering**: GPU rendering is client-side only. Servers don't see custom visuals.

3. **Learning Mode**: AI learning is experimental and may suggest suboptimal optimizations.

4. **Multiplayer**: All players must have the mod installed for full visual effects.

---

## 📝 License

MIT License - See LICENSE file for details.

---

## 🤝 Contributing

Contributions are welcome! Please submit pull requests or issues on GitHub.

---

## 📮 Support

For questions or issues:
- GitHub Issues: https://github.com/cadmium-mod/redstone-overhaul/issues
- Discord: https://discord.gg/cadmium-mod

---

*Version 0.0.1-alpha - April 2026*
