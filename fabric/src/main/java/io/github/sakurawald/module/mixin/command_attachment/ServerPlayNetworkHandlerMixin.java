package io.github.sakurawald.module.mixin.command_attachment;

import io.github.sakurawald.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.module.initializer.command_attachment.CommandAttachmentInitializer;
import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Inject(method = "swingHand", at = @At("HEAD"))
    void onPlayerLeftClick(Hand hand, CallbackInfo ci) {
        if (hand.equals(Hand.MAIN_HAND)) {
            String uuid = UuidHelper.getAttachedUuid(player.getMainHandStack().get(DataComponentTypes.CUSTOM_DATA));
            if (uuid == null) return;

            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            CommandAttachmentInitializer.triggerAttachmentModel(uuid, player, List.of(InteractType.LEFT, InteractType.BOTH));
        }

    }

}
