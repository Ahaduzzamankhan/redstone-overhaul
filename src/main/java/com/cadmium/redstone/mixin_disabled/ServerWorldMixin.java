package com.cadmium.redstone.mixin;

import com.cadmium.redstone.RedstoneOverhaulMod;
import com.cadmium.redstone.world.WorldAttachment;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for ServerLevel to handle level loading/unloading events.
 */
@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin {

    /**
     * Inject into tick method to ensure level attachment exists.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        try {
            ServerLevel level = (ServerLevel) (Object) this;
            // Ensure level attachment is created
            RedstoneOverhaulMod.getEngine().getWorldAttachment(level);
        } catch (Exception e) {
            // Silently fail - level may not be ready yet
        }
    }
}
