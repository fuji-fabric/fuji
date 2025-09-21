package io.github.sakurawald.fuji.module.mixin.core.event;


import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.server.tick.ServerTickStartEvent;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(MinecraftServer.class)
public class ServerTickStartEventMixin {

    @EventProducer(ServerTickStartEvent.class)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V"), method = "tick")
    void produceServerTickStartEvent(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerTickStartEvent event = new ServerTickStartEvent(server);
        EventManager.dispatchEvent(ServerTickStartEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
