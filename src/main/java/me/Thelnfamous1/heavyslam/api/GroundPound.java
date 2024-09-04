package me.Thelnfamous1.heavyslam.api;

import me.Thelnfamous1.heavyslam.HeavySlamMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Predicate;

public class GroundPound {
    public static GroundPound fromNetwork(FriendlyByteBuf buf) {
        return new GroundPound();
    }

    public void toNetwork(FriendlyByteBuf buf) {

    }

    public void onImpact(LivingEntity groundPounder) {
        float radius = 5 + Mth.clamp(groundPounder.fallDistance, 0, 5);
        if(!groundPounder.level.isClientSide){
            float damage = 8 + Mth.floor(groundPounder.fallDistance * 2);
            damage = Mth.floor(damage * 0.9F); // Manually reduce damage by 10%
            shockwave(groundPounder.level, groundPounder, radius, damage);
            groundPounder.level.playSound(
                    null, groundPounder.getX(), groundPounder.getY(), groundPounder.getZ(), SoundEvents.GENERIC_EXPLODE, groundPounder.getSoundSource(), 1.0F, 1.0F
            );
        } else{
            spawnSmashAttackParticles(groundPounder.level, groundPounder.getOnPos(), 750, radius);
        }
    }

    private static void spawnSmashAttackParticles(LevelAccessor pLevel, BlockPos pPos, int pPower, double radius) {
        Vec3 centerPos = Vec3.atCenterOf(pPos).add(0.0, 0.5, 0.0);
        ParticleOptions dustParticle = new BlockParticleOption(HeavySlamMod.DUST_PILLAR.get(), pLevel.getBlockState(pPos));

        for (int i = 0; (float)i < (float)pPower / 3.0F; i++) {
            double x = centerPos.x + pLevel.getRandom().nextGaussian() / 2.0;
            double y = centerPos.y;
            double z = centerPos.z + pLevel.getRandom().nextGaussian() / 2.0;
            double xSpeed = pLevel.getRandom().nextGaussian() * 0.2F;
            double ySpeed = pLevel.getRandom().nextGaussian() * 0.2F;
            double zSpeed = pLevel.getRandom().nextGaussian() * 0.2F;
            pLevel.addParticle(dustParticle, x, y, z, xSpeed, ySpeed, zSpeed);
        }

        for (int j = 0; (float)j < (float)pPower / 1.5F; j++) {
            double x = centerPos.x + radius * Math.cos(j) + pLevel.getRandom().nextGaussian() / 2.0;
            double y = centerPos.y;
            double z = centerPos.z + radius * Math.sin(j) + pLevel.getRandom().nextGaussian() / 2.0;
            double xSpeed = pLevel.getRandom().nextGaussian() * 0.05F;
            double ySpeed = pLevel.getRandom().nextGaussian() * 0.05F;
            double zSpeed = pLevel.getRandom().nextGaussian() * 0.05F;
            pLevel.addParticle(dustParticle, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    private static void shockwave(Level pLevel, LivingEntity groundPounder, double radius, float damage) {
        pLevel.getEntitiesOfClass(LivingEntity.class, groundPounder.getBoundingBox().inflate(radius), shockwavePredicate(groundPounder, radius))
                .forEach(target -> {
                    // damage
                    if(HeavySlamMod.DEBUG_GROUND_POUND)
                        HeavySlamMod.LOGGER.info("Applying shockwave damage of {} to {}", damage, target);
                    target.hurt(groundPounder instanceof Player player ? DamageSource.playerAttack(player) : DamageSource.mobAttack(groundPounder), damage);
                    // knockback
                    Vec3 distanceVec = target.position().subtract(groundPounder.position());
                    double knockbackPower = getKnockbackPower(groundPounder, target, distanceVec, radius);
                    knockbackPower *= 0.9F; // Manually reduce knockback by 10%
                    if(HeavySlamMod.DEBUG_GROUND_POUND)
                        HeavySlamMod.LOGGER.info("Applying shockwave knockback of {} to {}", knockbackPower, target);
                    Vec3 knockbackVec = distanceVec.normalize().scale(knockbackPower);
                    if (knockbackPower > 0.0) {
                        target.push(knockbackVec.x, 0.7F, knockbackVec.z);
                        if (target instanceof ServerPlayer serverplayer) {
                            serverplayer.connection.send(new ClientboundSetEntityMotionPacket(serverplayer));
                        }
                    }
                });
    }

    private static Predicate<LivingEntity> shockwavePredicate(LivingEntity groundPounder, double radius) {
        return target -> {
            boolean notSpectator;
            boolean notSelf;
            boolean notAlly;
            boolean flag;
            attackable: {
                notSpectator = !target.isSpectator();
                notSelf = target != groundPounder;
                notAlly = !groundPounder.isAlliedTo(target);
                if (target instanceof OwnableEntity ownableEntity && groundPounder.getUUID().equals(ownableEntity.getOwnerUUID())) {
                    flag = true;
                    break attackable;
                }

                flag = false;
            }

            boolean flag1;
            armorStand: {
                flag1 = !flag;
                if (target instanceof ArmorStand armorstand && armorstand.isMarker()) {
                    flag = false;
                    break armorStand;
                }

                flag = true;
            }

            boolean flag2 = flag;
            boolean inRange = groundPounder.distanceToSqr(target) <= Mth.square(radius);
            boolean canSee = groundPounder.hasLineOfSight(target);
            boolean isNotPassive = (!(target instanceof AgeableMob) && !(target instanceof WaterAnimal)) || target instanceof Enemy;
            boolean isAggressive = isAngryAt(target, groundPounder).orElse(true);
            return notSpectator && notSelf && notAlly && flag1 && flag2 && inRange && canSee && isNotPassive && isAggressive;
        };
    }

    private static Optional<Boolean> isAngryAt(LivingEntity entity, LivingEntity target) {
        if(entity instanceof NeutralMob neutralMob){
            return Optional.of(neutralMob.isAngryAt(target));
        }
        if(entity.getBrain().hasMemoryValue(MemoryModuleType.ANGRY_AT)){
            return entity.getBrain().getMemory(MemoryModuleType.ANGRY_AT)
                    .map(angryAt -> target.getUUID().equals(angryAt));
        }
        return Optional.empty();
    }

    private static double getKnockbackPower(LivingEntity groundPounder, LivingEntity target, Vec3 distanceVec, double radius) {
        return (radius - distanceVec.length())
                * 0.7F
                * (double)(groundPounder.fallDistance > 5.0F ? 2 : 1)
                * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }
}
