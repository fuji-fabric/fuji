package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.server.metadata.ModifyServerMetadataEvent;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@PhasedMixinTemplate
@Mixin(ServerStatusPacketListenerImpl.class)
public abstract class ModifyServerMetadataEventMixin {

    @EventProducer(ModifyServerMetadataEvent.class)
    @ModifyArg(method = "handleStatusRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;<init>(Lnet/minecraft/network/protocol/status/ServerStatus;)V"))
    public @NotNull ServerStatus produceModifyServerMetadataEvent(ServerStatus original) {
        ModifyServerMetadataEvent event = new ModifyServerMetadataEvent(original);
        EventManager.dispatchEvent(ModifyServerMetadataEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getServerMetadata();
    }

}
