package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerWorldChangedEvent extends BaseEvent {
    @NotNull ServerPlayerEntity player;
    @NotNull ServerWorld oldWorld;
    @NotNull ServerWorld newWorld;
}
