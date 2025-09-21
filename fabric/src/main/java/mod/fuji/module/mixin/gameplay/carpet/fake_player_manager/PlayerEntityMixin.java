package mod.fuji.module.mixin.gameplay.carpet.fake_player_manager;

import carpet.patches.EntityPlayerMPFake;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.gameplay.carpet.fake_player_manager.service.FakePlayerManagerService;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


// NOTE: The carpet-fabric use the default mixin priority, which is 1000.
@Mixin(value = PlayerEntity.class, priority = 999)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(@NotNull EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void validateAuthorityOnFakePlayerInteraction(Entity target, Hand hand, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        if (!(target instanceof EntityPlayerMPFake fakePlayer)) {
            return;
        }

        ServerPlayerEntity source = (ServerPlayerEntity) (Object) this;
        String fakePlayerName = PlayerHelper.getPlayerName(fakePlayer);
        if (!FakePlayerManagerService.isMyFakePlayer(source, fakePlayerName)) {
            cir.setReturnValue(ActionResult.FAIL);

            if (hand == Hand.MAIN_HAND) {
                TextHelper.sendTextByKey(source, "fake_player_manager.manipulate.forbidden");
            }
        }
    }

}
