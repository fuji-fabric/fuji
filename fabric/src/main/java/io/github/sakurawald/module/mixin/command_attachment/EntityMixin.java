package io.github.sakurawald.module.mixin.command_attachment;

import io.github.sakurawald.module.initializer.command_attachment.CommandAttachmentInitializer;
import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "interact", at = @At("HEAD"))
    void onPlayerRightClickEntity(@NotNull PlayerEntity player, Hand hand, @NotNull CallbackInfoReturnable<ActionResult> cir) {
        Entity entity = (Entity) (Object) this;

        if (hand == Hand.MAIN_HAND) {
            String uuid = entity.getUuidAsString();
            if (!CommandAttachmentInitializer.existsAttachmentModel(uuid)) return;

            CommandAttachmentInitializer.triggerAttachmentModel(uuid, player, List.of(InteractType.RIGHT, InteractType.BOTH));
        }
    }
}
