package io.github.sakurawald.fuji.module.mixin.core.event;

import com.mojang.brigadier.CommandDispatcher;
import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.server.command.OnCommandRegistrationEvent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(CommandManager.class)
public class CommandManagerEventMixin {

    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @EventProducer(OnCommandRegistrationEvent.class)
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V", remap = false), method = "<init>")
    void produceOnCommandRegistrationEvent(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess registryAccess, CallbackInfo ci) {
        CommandManager commandManager = (CommandManager) (Object) this;
        OnCommandRegistrationEvent event = new OnCommandRegistrationEvent(commandManager, this.dispatcher, registryAccess, environment);
        EventManager.dispatchEvent(OnCommandRegistrationEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

}
