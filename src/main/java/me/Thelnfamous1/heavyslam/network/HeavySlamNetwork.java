package me.Thelnfamous1.heavyslam.network;

import me.Thelnfamous1.heavyslam.HeavySlamMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class HeavySlamNetwork {
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(HeavySlamMod.MODID, "sync_channel");
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel SYNC_CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME, () -> "1.0",
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int index;

    public static void register(){
        SYNC_CHANNEL.registerMessage(index++, ClientboundSyncGroundPoundPacket.class, ClientboundSyncGroundPoundPacket::encode, ClientboundSyncGroundPoundPacket::new, ClientboundSyncGroundPoundPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
