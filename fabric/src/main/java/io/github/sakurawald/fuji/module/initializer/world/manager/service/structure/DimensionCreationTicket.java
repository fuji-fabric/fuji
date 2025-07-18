package io.github.sakurawald.fuji.module.initializer.world.manager.service.structure;

import io.github.sakurawald.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import lombok.Data;
import net.minecraft.server.command.ServerCommandSource;

@Data
public class DimensionCreationTicket {
    public final ServerCommandSource source;
    public final RuntimeDimensionDescriptor descriptor;
}
