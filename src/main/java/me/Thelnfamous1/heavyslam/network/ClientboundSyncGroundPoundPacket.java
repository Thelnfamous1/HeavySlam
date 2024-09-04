package me.Thelnfamous1.heavyslam.network;

import me.Thelnfamous1.heavyslam.api.GroundPound;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ClientboundSyncGroundPoundPacket {

    private final int entityId;
    @Nullable
    private final GroundPound groundPound;

    public ClientboundSyncGroundPoundPacket(int entityId, @Nullable GroundPound groundPound){
        this.entityId = entityId;
        this.groundPound = groundPound;
    }

    public ClientboundSyncGroundPoundPacket(FriendlyByteBuf buf){
        this.entityId = buf.readVarInt();
        this.groundPound = buf.readNullable(GroundPound::fromNetwork);
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeVarInt(this.entityId);
        buf.writeNullable(this.groundPound, (b, gp) -> gp.toNetwork(b));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
           ClientNetworkHandler.handleSyncGroundPound(this.entityId, this.groundPound);
        });
        ctx.get().setPacketHandled(true);
    }
}
