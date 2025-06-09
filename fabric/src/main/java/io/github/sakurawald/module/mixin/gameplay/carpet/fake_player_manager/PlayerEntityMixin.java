package io.github.sakurawald.module.mixin.gameplay.carpet.fake_player_manager;

import carpet.patches.EntityPlayerMPFake;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.gameplay.carpet.fake_player_manager.FakePlayerManagerInitializer;
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


// the carpet-fabric default event handler priority is 1000
@Mixin(value = PlayerEntity.class, priority = 999)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(@NotNull EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void canManipulateTheFakePlayer(Entity target, Hand hand, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        if (target instanceof EntityPlayerMPFake fakePlayer) {
            ServerPlayerEntity source = (ServerPlayerEntity) (Object) this;

            if (!FakePlayerManagerInitializer.isMyFakePlayer(source, fakePlayer)) {
                cir.setReturnValue(ActionResult.FAIL);

                if (hand == Hand.MAIN_HAND) {
                    TextHelper.sendMessageByKey(source, "fake_player_manager.manipulate.forbidden");
                }
            }
        }
    }

}
