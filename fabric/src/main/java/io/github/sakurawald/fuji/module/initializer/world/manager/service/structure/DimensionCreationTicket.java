package io.github.sakurawald.fuji.module.initializer.world.manager.service.structure;

import io.github.sakurawald.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import lombok.Value;
import net.minecraft.server.command.ServerCommandSource;

@Value
public class DimensionCreationTicket {
    public ServerCommandSource source;
    public RuntimeDimensionDescriptor descriptor;
}
