package me.Thelnfamous1.heavyslam.mixin;

import me.Thelnfamous1.heavyslam.api.GroundPounder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class EntityMixin {

    @Shadow public Level level;

    @Inject(method = "resetFallDistance", at = @At("RETURN"))
    private void post_resetFallDistance(CallbackInfo ci){
        if(!this.level.isClientSide && this instanceof GroundPounder groundPounder){
            groundPounder.heavyslam$setGroundPound(null);
        }
    }
}
