package mod.fuji.module.initializer.world.manager.service.structure;

import lombok.Value;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

@Value
public class DimensionDeletionTicket {

    public ServerCommandSource source;
    public ServerWorld world;
    public boolean deleteWorldFiles;
    public boolean deleteRuntimeDimensionDescriptor;

}
