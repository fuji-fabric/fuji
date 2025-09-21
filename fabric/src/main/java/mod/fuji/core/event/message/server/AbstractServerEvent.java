package mod.fuji.core.event.message.server;

import mod.fuji.core.event.message.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class AbstractServerEvent extends BaseEvent {
    @NotNull MinecraftServer server;
}
