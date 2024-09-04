package me.Thelnfamous1.heavyslam.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.Thelnfamous1.heavyslam.api.GroundPounder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow protected M model;

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/EntityModel;riding:Z", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER, ordinal = 0))
    private void modify_riding_render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci){
        if(pEntity instanceof GroundPounder groundPounder && groundPounder.heavyslam$getGroundPound() != null){
            this.model.riding = true;
        }
    }
}
