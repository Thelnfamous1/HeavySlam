package me.Thelnfamous1.heavyslam.api;

import me.Thelnfamous1.heavyslam.HeavySlamMod;
import me.Thelnfamous1.heavyslam.config.HeavySlamConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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
        double radius = getAttackRadius(groundPounder);
        if(!groundPounder.level.isClientSide){
            if(HeavySlamMod.DEBUG_GROUND_POUND)
                HeavySlamMod.LOGGER.info("{} fell for {} blocks and created a shockwave of radius {} blocks!", groundPounder, groundPounder.fallDistance, radius);
            shockwave(groundPounder.level, groundPounder, radius);
            groundPounder.level.playSound(
                    null, groundPounder.getX(), groundPounder.getY(), groundPounder.getZ(), HeavySlamMod.GROUND_POUND_IMPACT.get(), groundPounder.getSoundSource(), (float)radius, 1.0F
            );
        } else{
            spawnSmashAttackParticles(groundPounder.level, groundPounder.getOnPos(), 750, radius);
        }
    }

    private static double getAttackRadius(LivingEntity groundPounder) {
        double attackRadius = Mth.clamp(
                HeavySlamConfig.SERVER.groundPoundRadiusBase.get() + (groundPounder.fallDistance * HeavySlamConfig.SERVER.groundPoundRadiusFallDistanceScale.get()),
                0,
                HeavySlamConfig.SERVER.groundPoundRadiusMax.get());
        if(HeavySlamConfig.SERVER.groundPoundRadiusFloor.get()){
            return Mth.floor(attackRadius);
        } else{
            return attackRadius;
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

    private static void shockwave(Level pLevel, LivingEntity groundPounder, double radius) {
        pLevel.getEntitiesOfClass(LivingEntity.class, groundPounder.getBoundingBox().inflate(radius), shockwavePredicate(groundPounder, radius))
                .forEach(target -> {
                    Vec3 distanceVec = target.position().subtract(groundPounder.position());
                    double distanceToTarget = distanceVec.length();
                    // damage
                    float damage = getAttackDamage(groundPounder, target, radius, distanceToTarget);
                    if(HeavySlamMod.DEBUG_GROUND_POUND)
                        HeavySlamMod.LOGGER.info("Applying shockwave damage of {} to {}", damage, target);
                    target.hurt(groundPounder instanceof Player player ? DamageSource.playerAttack(player) : DamageSource.mobAttack(groundPounder), damage);
                    // knockback
                    double knockbackPower = getKnockbackPower(groundPounder, target, radius, distanceToTarget);
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
        return hitTarget -> {
            boolean notSpectator;
            boolean notSelf;
            boolean notAlly;
            boolean flag;
            attackable: {
                notSpectator = !hitTarget.isSpectator();
                notSelf = hitTarget != groundPounder;
                notAlly = !groundPounder.isAlliedTo(hitTarget);
                if (hitTarget instanceof OwnableEntity ownableEntity && groundPounder.getUUID().equals(ownableEntity.getOwnerUUID())) {
                    flag = true;
                    break attackable;
                }

                flag = false;
            }

            boolean flag1;
            armorStand: {
                flag1 = !flag;
                if (hitTarget instanceof ArmorStand armorstand && armorstand.isMarker()) {
                    flag = false;
                    break armorStand;
                }

                flag = true;
            }

            boolean flag2 = flag;
            boolean inRange = groundPounder.distanceToSqr(hitTarget) <= Mth.square(radius);
            boolean canSee = groundPounder.hasLineOfSight(hitTarget);
            boolean isNotPassive = (!(hitTarget instanceof AgeableMob) && !(hitTarget instanceof WaterAnimal)) || hitTarget instanceof Enemy;
            Optional<Boolean> isAngryAt = isAngryAt(hitTarget, groundPounder);
            boolean isAggressive = isAngryAt.orElse(true);
            if(!isNotPassive && isAngryAt.orElse(false)){
                isNotPassive = true;
            }
            return notSpectator && notSelf && notAlly && flag1 && flag2 && inRange && canSee && isNotPassive && isAggressive;
        };
    }

    private static Optional<Boolean> isAngryAt(LivingEntity entity, LivingEntity target) {
        if(entity instanceof NeutralMob neutralMob){
            return Optional.of((target.getType() == EntityType.PLAYER && neutralMob.isAngryAtAllPlayers(target.level)) || target.getUUID().equals(neutralMob.getPersistentAngerTarget()));
        }
        if(entity.getBrain().hasMemoryValue(MemoryModuleType.ANGRY_AT)){
            return entity.getBrain().getMemory(MemoryModuleType.ANGRY_AT)
                    .map(angryAt -> {
                        boolean universal = false;
                        if(entity.getBrain().hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)){
                            universal = target.getType() == EntityType.PLAYER && entity.getBrain().getMemory(MemoryModuleType.UNIVERSAL_ANGER).orElse(false);
                        }
                        return universal || target.getUUID().equals(angryAt);
                    });
        }
        return Optional.empty();
    }

    private static float getAttackDamage(LivingEntity groundPounder, LivingEntity target, double radius, double distanceToTarget){
        double distanceFromRadius = radius - distanceToTarget;
        double attackDamage = Mth.clamp(
                HeavySlamConfig.SERVER.groundPoundDamageBase.get() + (distanceFromRadius * HeavySlamConfig.SERVER.groundPoundDamageScale.get()),
                0,
                HeavySlamConfig.SERVER.groundPoundDamageMax.get());
        if(HeavySlamConfig.SERVER.groundPoundDamageFloor.get()){
            return Mth.floor(attackDamage);
        } else {
            return (float) attackDamage;
        }
    }

    private static double getKnockbackPower(LivingEntity groundPounder, LivingEntity target, double radius, double distanceToTarget) {
        double distanceFromRadius = radius - distanceToTarget;
        double knockbackPower = Mth.clamp(
                HeavySlamConfig.SERVER.groundPoundKnockbackBase.get() + (distanceFromRadius * HeavySlamConfig.SERVER.groundPoundKnockbackScale.get()),
                0,
                HeavySlamConfig.SERVER.groundPoundDamageMax.get());
        if(HeavySlamConfig.SERVER.groundPoundKnockbackResistance.get()){
            knockbackPower *= (1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        }
        if(HeavySlamConfig.SERVER.groundPoundKnockbackFloor.get()){
            return Mth.floor(knockbackPower);
        } else{
            return knockbackPower;
        }
    }
}
