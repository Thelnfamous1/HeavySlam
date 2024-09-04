package me.Thelnfamous1.heavyslam.client;

import me.Thelnfamous1.heavyslam.HeavySlamMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = HeavySlamMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HeavySlamClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event){

    }

    @SubscribeEvent
    public static void registerParticleProvider(RegisterParticleProvidersEvent event){
        event.register(HeavySlamMod.DUST_PILLAR.get(), pSprites -> new CustomTerrainParticle.DustPillarProvider());
    }

    /*
    @Mod.EventBusSubscriber(modid = HeavySlamMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class Gameplay{

    }
     */
}
