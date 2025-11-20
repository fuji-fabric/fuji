package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerWorldChangedEvent extends BaseEvent {
    @NotNull ServerPlayer player;
    @NotNull ServerLevel oldWorld;
    @NotNull ServerLevel newWorld;
}
