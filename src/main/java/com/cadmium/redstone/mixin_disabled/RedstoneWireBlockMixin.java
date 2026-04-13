package com.cadmium.redstone.mixin;

import com.cadmium.redstone.RedstoneOverhaulMod;
import com.cadmium.redstone.node.NodeType;
import com.cadmium.redstone.world.WorldAttachment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for RedStoneWireBlock to intercept vanilla redstone behavior
 * and redirect it to our custom simulation engine.
 * Note: Temporarily disabled for MC 26.1 compatibility - class structure changed
 */
// @Mixin(RedStoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {
    
    /**
     * Inject into getWeakRedstonePower to override vanilla power calculation.
     */
    @Inject(method = "getWeakRedstonePower", at = @At("HEAD"), cancellable = true)
    private void onGetWeakRedstonePower(BlockState state, BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        try {
            WorldAttachment attachment = getWorldAttachment((Level) level);
            if (attachment != null) {
                int power = attachment.getSignalStrength(pos);
                cir.setReturnValue(power);
            }
        } catch (Exception e) {
            RedstoneOverhaulMod.LOGGER.warn("Error in getWeakRedstonePower mixin: {}", e.getMessage());
        }
    }
    
    /**
     * Inject into neighborUpdate to intercept neighbor changes.
     */
    @Inject(method = "neighborUpdate", at = @At("HEAD"), cancellable = true)
    private void onNeighborUpdate(BlockState state, Level level, BlockPos pos, BlockState sourceState, Direction dir, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        try {
            WorldAttachment attachment = getWorldAttachment(level);
            if (attachment != null) {
                // Schedule update through our engine instead of vanilla behavior
                attachment.scheduleUpdate(pos, 1);
                ci.cancel();
            }
        } catch (Exception e) {
            RedstoneOverhaulMod.LOGGER.warn("Error in neighborUpdate mixin: {}", e.getMessage());
        }
    }
    
    /**
     * Inject into onBlockAdded to register the component with our engine.
     */
    @Inject(method = "onBlockAdded", at = @At("TAIL"))
    private void onBlockAdded(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        try {
            WorldAttachment attachment = getWorldAttachment(level);
            if (attachment != null) {
                attachment.addComponent(pos, NodeType.WIRE);
            }
        } catch (Exception e) {
            RedstoneOverhaulMod.LOGGER.warn("Error in onBlockAdded mixin: {}", e.getMessage());
        }
    }
    
    /**
     * Inject into onStateReplaced to remove the component from our engine.
     */
    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void onStateReplaced(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        try {
            if (state.getBlock() instanceof RedStoneWireBlock) {
                WorldAttachment attachment = getWorldAttachment(level);
                if (attachment != null) {
                    attachment.removeComponent(pos);
                }
            }
        } catch (Exception e) {
            RedstoneOverhaulMod.LOGGER.warn("Error in onStateReplaced mixin: {}", e.getMessage());
        }
    }
    
    /**
     * Helper method to get world attachment safely.
     */
    private WorldAttachment getWorldAttachment(Level level) {
        if (level == null) {
            return null;
        }

        try {
            return RedstoneOverhaulMod.getEngine().getWorldAttachment(level);
        } catch (Exception e) {
            return null;
        }
    }
}

