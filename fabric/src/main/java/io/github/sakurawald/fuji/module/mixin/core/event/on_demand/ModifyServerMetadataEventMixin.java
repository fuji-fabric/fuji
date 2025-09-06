package io.github.sakurawald.fuji.module.mixin.core.event.on_demand;

import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.impl.on_demand.ModifyServerMetadataEvent;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@PhasedMixinTemplate
@Mixin(ServerQueryNetworkHandler.class)
public abstract class ModifyServerMetadataEventMixin {

    @EventProducer(ModifyServerMetadataEvent.class)
    @ModifyArg(method = "onRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/query/QueryResponseS2CPacket;<init>(Lnet/minecraft/server/ServerMetadata;)V"))
    public @NotNull ServerMetadata produceModifyServerMetadataEvent(ServerMetadata original) {
        ModifyServerMetadataEvent event = new ModifyServerMetadataEvent(original);
        EventManager.dispatchEvent(ModifyServerMetadataEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getServerMetadata();
    }

}
