package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class PlayerTeleportPreEvent extends BaseEvent {
    @NotNull CallbackInfo callbackInfo;
    @NotNull ServerPlayerEntity player;
    @NotNull ServerWorld destinationDimension;
    double destinationX;
    double destinationY;
    double destinationZ;
    float destinationYaw;
    float destinationPitch;
    @NotNull Set<PositionFlag> positionFlags;
}
