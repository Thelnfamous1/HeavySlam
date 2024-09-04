package me.Thelnfamous1.heavyslam.network;

import me.Thelnfamous1.heavyslam.api.GroundPound;
import me.Thelnfamous1.heavyslam.api.GroundPounder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class ClientNetworkHandler {
    public static void handleSyncGroundPound(int entityId, @Nullable GroundPound groundPound) {
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if(entity instanceof GroundPounder groundPounder){
            groundPounder.heavyslam$setGroundPound(groundPound);
        }
    }
}
