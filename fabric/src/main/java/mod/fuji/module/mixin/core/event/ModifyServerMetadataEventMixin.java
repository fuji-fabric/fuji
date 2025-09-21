package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.server.metadata.ModifyServerMetadataEvent;
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
