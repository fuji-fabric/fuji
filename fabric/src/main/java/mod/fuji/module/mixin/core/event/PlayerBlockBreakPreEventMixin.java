package mod.fuji.module.mixin.core.event;


import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerBlockBreakPreEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@PhasedMixinTemplate
@Mixin(ServerPlayerGameMode.class)
public class PlayerBlockBreakPreEventMixin {

    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @EventProducer(PlayerBlockBreakPreEvent.class)
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    void producePlayerBlockBreakPreEvent(BlockPos blockPos, @NotNull CallbackInfoReturnable<Boolean> cir) {
        PlayerBlockBreakPreEvent event = new PlayerBlockBreakPreEvent(player, level, blockPos, cir);
        EventManager.dispatchEvent(PlayerBlockBreakPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }
}
