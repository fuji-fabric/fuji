package io.github.sakurawald.fuji.module.initializer.world.manager.service.structure;

import lombok.Data;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

@Data
public class DimensionDeletionTicket {

    public final ServerCommandSource source;
    public final ServerWorld world;
    public final boolean deleteWorldFiles;
    public final boolean deleteRuntimeDimensionDescriptor;

}
