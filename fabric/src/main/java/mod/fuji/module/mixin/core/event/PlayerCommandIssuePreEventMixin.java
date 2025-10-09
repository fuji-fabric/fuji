package mod.fuji.module.mixin.core.event;

import com.llamalad7.mixinextras.sugar.Cancellable;
import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerCommandIssuePreEvent;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(value = ServerPlayNetworkHandler.class)
public class PlayerCommandIssuePreEventMixin {

    @Shadow
    public ServerPlayerEntity player;

    @EventProducer(PlayerCommandIssuePreEvent.class)
    #if MC_VER <= MC_1_20_4
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "handleCommandExecution", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/CommandExecutionC2SPacket;comp_808()Ljava/lang/String;"))
    #elif MC_VER > MC_1_20_4
    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "executeCommand", at = @At(value = "HEAD"), argsOnly = true)
    #endif
    String producePlayerCommandIssuePreEvent(@NotNull String commandString, @Cancellable CallbackInfo callbackInfo) {
        PlayerCommandIssuePreEvent event = new PlayerCommandIssuePreEvent(player, commandString, callbackInfo);
        EventManager.dispatchEvent(PlayerCommandIssuePreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
        return event.getCommandString();
    }

}
