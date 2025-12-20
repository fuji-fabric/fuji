package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.core.structure.RelativeFlagsIR;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class PlayerTeleportPreEvent extends BaseEvent {
    @NotNull CallbackInfo callbackInfo;
    @NotNull ServerPlayer player;
    @NotNull ServerLevel destinationDimension;
    double destinationX;
    double destinationY;
    double destinationZ;
    float destinationYaw;
    float destinationPitch;
    RelativeFlagsIR relativeFlagsIR;
}
