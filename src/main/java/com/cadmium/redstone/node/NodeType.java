package com.cadmium.redstone.node;

/**
 * Enumeration of all redstone node types supported by the overhaul system.
 */
public enum NodeType {
    // Basic components
    WIRE("Redstone Wire"),
    REPEATER("Redstone Repeater"),
    COMPARATOR("Redstone Comparator"),
    TORCH("Redstone Torch"),
    
    // Logic gates
    AND_GATE("AND Gate"),
    OR_GATE("OR Gate"),
    XOR_GATE("XOR Gate"),
    NAND_GATE("NAND Gate"),
    NOR_GATE("NOR Gate"),
    NOT_GATE("NOT Gate"),
    XNOR_GATE("XNOR Gate"),
    
    // Memory components
    MEMORY_CELL("Memory Cell (RS Latch)"),
    T_FLIP_FLOP("T Flip-Flop"),
    D_FLIP_FLOP("D Flip-Flop"),
    J_K_FLIP_FLOP("JK Flip-Flop"),
    
    // Advanced components
    COUNTER("Counter"),
    TIMER("Timer"),
    MULTIPLEXER("Multiplexer"),
    DEMULTIPLEXER("Demultiplexer"),
    ENCODER("Encoder"),
    DECODER("Decoder"),
    
    // I/O
    LEVER("Lever"),
    BUTTON("Button"),
    PRESSURE_PLATE("Pressure Plate"),
    TRIPWIRE("Tripwire"),
    OBSERVER("Observer"),
    PISTON("Piston"),
    STICKY_PISTON("Sticky Piston"),
    REDSTONE_LAMP("Redstone Lamp"),
    DROPPER("Dropper"),
    DISPENSER("Dispenser"),
    HOPPER("Hopper"),
    NOTE_BLOCK("Note Block"),
    DOOR("Door"),
    TRAPDOOR("Trapdoor"),
    FENCE_GATE("Fence Gate"),
    RAIL("Powered Rail");
    
    private final String displayName;
    
    NodeType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this is a logic gate type.
     */
    public boolean isLogicGate() {
        return this == AND_GATE || this == OR_GATE || this == XOR_GATE || 
               this == NAND_GATE || this == NOR_GATE || this == NOT_GATE || this == XNOR_GATE;
    }
    
    /**
     * Check if this is a memory element.
     */
    public boolean isMemoryElement() {
        return this == MEMORY_CELL || this == T_FLIP_FLOP || this == D_FLIP_FLOP || this == J_K_FLIP_FLOP;
    }
    
    /**
     * Check if this is an input device.
     */
    public boolean isInputDevice() {
        return this == LEVER || this == BUTTON || this == PRESSURE_PLATE || 
               this == TRIPWIRE || this == OBSERVER;
    }
    
    /**
     * Check if this is an output device.
     */
    public boolean isOutputDevice() {
        return this == PISTON || this == STICKY_PISTON || this == REDSTONE_LAMP || 
               this == DROPPER || this == DISPENSER || this == NOTE_BLOCK || this == DOOR || 
               this == TRAPDOOR || this == FENCE_GATE || this == RAIL;
    }
}

