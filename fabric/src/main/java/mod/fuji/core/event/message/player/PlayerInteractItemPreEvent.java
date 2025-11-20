package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerInteractItemPreEvent extends BaseEvent {
    @NotNull ServerPlayer player;
    @NotNull Level world;
    @NotNull ItemStack itemStack;
    @NotNull InteractionHand hand;
    @NotNull CallbackInfoReturnable<InteractionResult> callbackInfoReturnable;
}
