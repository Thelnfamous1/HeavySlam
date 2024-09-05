package me.Thelnfamous1.heavyslam;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;
import me.Thelnfamous1.heavyslam.api.GroundPound;
import me.Thelnfamous1.heavyslam.api.GroundPounder;
import me.Thelnfamous1.heavyslam.config.HeavySlamConfig;
import me.Thelnfamous1.heavyslam.network.HeavySlamNetwork;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.function.Function;

@Mod(HeavySlamMod.MODID)
@Mod.EventBusSubscriber(modid = HeavySlamMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HeavySlamMod {
    public static final String MODID = "heavyslam";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean DEBUG_GROUND_POUND = !FMLEnvironment.production;

    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);

    public static final RegistryObject<ParticleType<BlockParticleOption>> DUST_PILLAR = registerComplexParticleType("dust_pillar", BlockParticleOption.DESERIALIZER, BlockParticleOption::codec);

    private static <T extends ParticleOptions> RegistryObject<ParticleType<T>> registerComplexParticleType(String name, ParticleOptions.Deserializer<T> deserializer, Function<ParticleType<T>, Codec<T>> pCodecFactory) {
        return PARTICLE_TYPES.register(name, () -> new ParticleType<T>(false, deserializer) {
            public Codec<T> codec() {
                return pCodecFactory.apply(this);
            }
        });
    }

    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> GROUND_POUND_IMPACT = registerSoundEvent("ground_pound_impact");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(MODID, name)));
    }

    public HeavySlamMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        PARTICLE_TYPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, HeavySlamConfig.serverSpec);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(HeavySlamNetwork::register);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Gameplay{
        private static final Int2BooleanArrayMap HAD_GROUND_POUND_IMPULSE = new Int2BooleanArrayMap();


        @SubscribeEvent
        public static void onServerStarting(ServerStartedEvent event){
            HAD_GROUND_POUND_IMPULSE.clear();
        }

        @SubscribeEvent
        public static void onServerStopped(ServerStoppedEvent event){
            HAD_GROUND_POUND_IMPULSE.clear();
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event){
            if(event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER){
                boolean hadGroundPoundImpulse = HAD_GROUND_POUND_IMPULSE.getOrDefault(event.player.getId(), false);
                boolean hasGroundPoundImpulse = hasGroundPoundImpulse(event.player);
                if(hasGroundPoundImpulse && !hadGroundPoundImpulse){
                    GroundPounder groundPounder = (GroundPounder) event.player;
                    if(groundPounder.heavyslam$getGroundPound() == null){
                        groundPounder.heavyslam$setGroundPound(new GroundPound());
                        if(DEBUG_GROUND_POUND)
                            LOGGER.info("Starting ground pound for {}", event.player);
                    }
                }
                HAD_GROUND_POUND_IMPULSE.put(event.player.getId(), hasGroundPoundImpulse);
            }
        }

        private static boolean hasGroundPoundImpulse(Player player) {
            return player.isShiftKeyDown() && player.fallDistance > 1.5F && !player.isFallFlying();
        }

        @SubscribeEvent
        public static void onPlayerFall(PlayerFlyableFallEvent event){
            didGroundPound(event.getEntity());
        }

        private static boolean didGroundPound(LivingEntity entity) {
            GroundPounder groundPounder = (GroundPounder) entity;
            GroundPound groundPound = groundPounder.heavyslam$getGroundPound();
            if(groundPound != null){
                groundPound.onImpact(entity);
                if(!entity.level.isClientSide){
                    groundPounder.heavyslam$setGroundPound(null);
                    if(DEBUG_GROUND_POUND)
                        LOGGER.info("Ending ground pound for {}", entity);
                }
                return true;
            }
            return false;
        }

        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent event){
            if(didGroundPound(event.getEntity())){
                event.setCanceled(true);
            }
        }
    }
}
