package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerInteractItemPreEvent extends BaseEvent {
    @NotNull ServerPlayerEntity player;
    @NotNull World world;
    @NotNull ItemStack itemStack;
    @NotNull Hand hand;
    @NotNull CallbackInfoReturnable<ActionResult> callbackInfoReturnable;
}
