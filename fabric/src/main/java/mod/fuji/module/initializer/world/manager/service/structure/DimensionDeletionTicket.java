package mod.fuji.module.initializer.world.manager.service.structure;

import lombok.Value;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;

@Value
public class DimensionDeletionTicket {

    public CommandSourceStack source;
    public ServerLevel world;
    public boolean deleteWorldFiles;
    public boolean deleteRuntimeDimensionDescriptor;

}
