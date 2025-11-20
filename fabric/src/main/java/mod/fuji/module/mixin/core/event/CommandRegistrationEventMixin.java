package mod.fuji.module.mixin.core.event;

import com.mojang.brigadier.CommandDispatcher;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.server.command.CommandRegistrationEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(Commands.class)
public class CommandRegistrationEventMixin {

    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @EventProducer(CommandRegistrationEvent.class)
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V", remap = false), method = "<init>")
    void produceOnCommandRegistrationEvent(Commands.CommandSelection environment, CommandBuildContext registryAccess, CallbackInfo ci) {
        Commands commandManager = (Commands) (Object) this;
        CommandRegistrationEvent event = new CommandRegistrationEvent(commandManager, this.dispatcher, registryAccess, environment);
        EventManager.dispatchEvent(CommandRegistrationEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

}
