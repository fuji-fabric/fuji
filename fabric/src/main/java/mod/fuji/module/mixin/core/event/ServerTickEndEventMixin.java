package mod.fuji.module.mixin.core.event;


import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.server.tick.ServerTickEndEvent;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(MinecraftServer.class)
public class ServerTickEndEventMixin {

    @EventProducer(ServerTickEndEvent.class)
    @Inject(at = @At(value = "TAIL", target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V"), method = "tickServer")
    void produceServerTickEndEvent(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerTickEndEvent event = new ServerTickEndEvent(server);
        EventManager.dispatchEvent(ServerTickEndEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

}
