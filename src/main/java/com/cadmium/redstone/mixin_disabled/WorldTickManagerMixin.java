package com.cadmium.redstone.mixin;

import com.cadmium.redstone.RedstoneOverhaulMod;
import com.cadmium.redstone.engine.RedstoneEngine;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for WorldTickManager to integrate our redstone engine into the world tick loop.
 */
@Mixin(targets = "net/minecraft/server/world/WorldTickManager")
public abstract class WorldTickManagerMixin {
    
    /**
     * Inject into tick method to call our redstone engine tick.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        try {
            RedstoneEngine engine = RedstoneOverhaulMod.getEngine();
            if (engine != null && engine.isRunning()) {
                engine.tick();
            }
        } catch (Exception e) {
            // Log but don't crash - vanilla behavior should continue
            System.err.println("[RedstoneOverhaul] Error during engine tick: " + e.getMessage());
        }
    }
}

