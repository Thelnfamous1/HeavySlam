package me.Thelnfamous1.heavyslam.mixin;

import me.Thelnfamous1.heavyslam.api.GroundPound;
import me.Thelnfamous1.heavyslam.api.GroundPounder;
import me.Thelnfamous1.heavyslam.network.ClientboundSyncGroundPoundPacket;
import me.Thelnfamous1.heavyslam.network.HeavySlamNetwork;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements GroundPounder {
    @Unique
    @Nullable
    private GroundPound heavyslam$groundPound;
    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public @Nullable GroundPound heavyslam$getGroundPound() {
        return this.heavyslam$groundPound;
    }

    @Override
    public void heavyslam$setGroundPound(@Nullable GroundPound groundPound) {
        boolean changed = this.heavyslam$groundPound != groundPound;
        this.heavyslam$groundPound = groundPound;
        if(!this.level.isClientSide && changed){
            HeavySlamNetwork.SYNC_CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this), new ClientboundSyncGroundPoundPacket(this.getId(), this.heavyslam$groundPound));
        }
    }
}
