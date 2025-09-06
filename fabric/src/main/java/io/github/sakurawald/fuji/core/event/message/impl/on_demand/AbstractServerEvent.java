package io.github.sakurawald.fuji.core.event.message.impl.on_demand;

import io.github.sakurawald.fuji.core.event.message.abst.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class AbstractServerEvent extends BaseEvent {
    @NotNull MinecraftServer server;
}
