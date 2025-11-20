package mod.fuji.module.mixin.gameplay.carpet.fake_player_manager;

import carpet.patches.EntityPlayerMPFake;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.gameplay.carpet.fake_player_manager.service.FakePlayerManagerService;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


// NOTE: The carpet-fabric use the default mixin priority, which is 1000.
@Mixin(value = Player.class, priority = 999)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(@NotNull EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void validateAuthorityOnFakePlayerInteraction(Entity target, InteractionHand hand, @NotNull CallbackInfoReturnable<InteractionResult> cir) {
        if (!(target instanceof EntityPlayerMPFake fakePlayer)) {
            return;
        }

        ServerPlayer source = (ServerPlayer) (Object) this;
        String fakePlayerName = PlayerHelper.getPlayerName(fakePlayer);
        if (!FakePlayerManagerService.isMyFakePlayer(source, fakePlayerName)) {
            cir.setReturnValue(InteractionResult.FAIL);

            if (hand == InteractionHand.MAIN_HAND) {
                TextHelper.sendTextByKey(source, "fake_player_manager.manipulate.forbidden");
            }
        }
    }

}
