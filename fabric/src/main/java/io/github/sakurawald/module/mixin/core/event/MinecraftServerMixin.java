package io.github.sakurawald.module.mixin.core.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.core.event.impl.ServerTickEvents;
import io.github.sakurawald.core.event.impl.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"), method = "runServer")
    private void beforeSetupServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STARTING.invoker().fire((MinecraftServer) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;", ordinal = 0), method = "runServer")
    private void afterSetupServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STARTED.invoker().fire((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "shutdown")
    private void afterShutdownServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STOPPED.invoker().fire((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "shutdown")
    private void beforeShutdownServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STOPPING.invoker().fire((MinecraftServer) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V"), method = "tick")
    private void onStartTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerTickEvents.START_SERVER_TICK.invoker().fire((MinecraftServer) (Object) this);
    }

    @SuppressWarnings("MixinExtrasOperationParameters")
    @WrapOperation(method = "createWorlds", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> V onLoadWorld(Map<K, V> worlds, K registryKey, V serverWorld, Operation<V> original) {
        final V result = original.call(worlds, registryKey, serverWorld);
        ServerWorldEvents.LOAD.invoker().fire((MinecraftServer) (Object) this, (ServerWorld) serverWorld);
        return result;
    }

    @SuppressWarnings("LocalCaptureFailException")
    @Inject(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;close()V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onUnloadWorldAtShutdown(CallbackInfo ci, Iterator<ServerWorld> worlds, ServerWorld world) {
        ServerWorldEvents.UNLOAD.invoker().fire((MinecraftServer) (Object) this, world);
    }

}
