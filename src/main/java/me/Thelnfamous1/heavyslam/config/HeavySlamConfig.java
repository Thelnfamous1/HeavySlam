package me.Thelnfamous1.heavyslam.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HeavySlamConfig {
    public static final ForgeConfigSpec serverSpec;
    public static final HeavySlamConfig.Server SERVER;
    static {
        final Pair<HeavySlamConfig.Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(HeavySlamConfig.Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();
    }
    public static class Server {

        public final ForgeConfigSpec.DoubleValue groundPoundRadiusBase;
        public final ForgeConfigSpec.DoubleValue groundPoundRadiusFallDistanceScale;
        public final ForgeConfigSpec.DoubleValue groundPoundRadiusMax;
        public final ForgeConfigSpec.BooleanValue groundPoundRadiusFloor;
        public final ForgeConfigSpec.DoubleValue groundPoundDamageScale;
        public final ForgeConfigSpec.DoubleValue groundPoundDamageBase;
        public final ForgeConfigSpec.DoubleValue groundPoundDamageMax;
        public final ForgeConfigSpec.BooleanValue groundPoundDamageFloor;
        public final ForgeConfigSpec.DoubleValue groundPoundKnockbackBase;
        public final ForgeConfigSpec.DoubleValue groundPoundKnockbackScale;
        public final ForgeConfigSpec.DoubleValue groundPoundKnockbackMax;
        public final ForgeConfigSpec.BooleanValue groundPoundKnockbackResistance;
        public final ForgeConfigSpec.BooleanValue groundPoundKnockbackFloor;

        Server(ForgeConfigSpec.Builder builder) {
            // Radius
            builder.comment("Ground pound radius configuration settings")
                    .push("ground_pound_radius");
            groundPoundRadiusBase = builder
                    .comment("Base value used for calculating the radius of ground pound attacks.")
                    .defineInRange("groundPoundRadiusBase", 5.0D, 0.0D, 100.0D);
            groundPoundRadiusFallDistanceScale = builder
                    .comment("The amount to scale the attacker's fall distance by when calculating the radius of ground pound attacks.\n " +
                            "The scaled fall distance will be added to the groundPoundRadiusBase value.")
                    .defineInRange("groundPoundRadiusFallDistanceScale", 1.0D, 0.0D, 100.0D);
            groundPoundRadiusMax = builder
                    .comment("The maximum amount the radius of ground pound attacks can be.")
                    .defineInRange("groundPoundRadiusMax", 10.0D, 0.0D, 1024.0D);
            groundPoundRadiusFloor = builder
                    .comment("Whether or not the calculated radius of a ground pound attack should be rounded down to the nearest whole number.")
                    .define("groundPoundRadiusFloor", true);
            builder.pop();
            // Damage
            builder.comment("Ground pound damage configuration settings")
                    .push("ground_pound_damage");
            groundPoundDamageBase = builder
                    .comment("Base value used for calculating the damage of ground pound attacks.")
                    .defineInRange("groundPoundDamageScale", 0.0D, 0.0D, 1024.0D);
            groundPoundDamageScale = builder
                    .comment("The amount to scale (the target's distance from the attack's radius) by when calculating the damage of ground pound attacks.\n" +
                            "The scaled distance will be added to the groundPoundDamageScale value.")
                    .defineInRange("groundPoundDamageScale", 1.0D, 0.0D, 100.0D);
            groundPoundDamageMax = builder
                    .comment("The maximum amount of damage that ground pound attacks can apply.")
                    .defineInRange("groundPoundDamageMax", 1024.0D, 0.0D, 1024.0D);
            groundPoundDamageFloor = builder
                    .comment("Whether or not the calculated damage of a ground pound attack should be rounded down to the nearest whole number.")
                    .define("groundPoundDamageFloor", true);
            builder.pop();
            // Knockback
            builder.comment("Ground pound knockback configuration settings")
                    .push("ground_pound_knockback");
            groundPoundKnockbackBase = builder
                    .comment("Base value used for calculating the knockback of ground pound attacks.")
                    .defineInRange("groundPoundKnockbackBase", 0.0D, 0.0D, 1024.0D);
            groundPoundKnockbackScale = builder
                    .comment("The amount to scale (the target's distance from the attack's radius) by when calculating the radius of ground pound attacks.\n" +
                            "The scaled distance will be added to the groundPoundKnockbackBase value.")
                    .defineInRange("groundPoundKnockbackScale", 0.5D, 0.0D, 100.0D);
            groundPoundKnockbackMax = builder
                    .comment("The maximum amount of knockback that ground pound attacks can apply.")
                    .defineInRange("groundPoundKnockbackMax", 1024.0D, 0.0D, 1024.0D);
            groundPoundKnockbackResistance = builder
                    .comment("Whether or not the calculated knockback of a ground pound attack should be scaled by (1 - the target's knockback resistance).")
                    .define("groundPoundKnockbackResistance", true);
            groundPoundKnockbackFloor = builder
                    .comment("Whether or not the calculated knockback of a ground pound attack should be rounded down to the nearest whole number.")
                    .define("groundPoundKnockbackFloor", true);
            builder.pop();
        }
    }
}
