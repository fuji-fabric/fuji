package mod.fuji.core.event.message.player;

import mod.fuji.core.event.message.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ModifyPlayerListNameEvent extends BaseEvent {

    @NotNull ServerPlayer player;
    @Nullable Component text;

}
