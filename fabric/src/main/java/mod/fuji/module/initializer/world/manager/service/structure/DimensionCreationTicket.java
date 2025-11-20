package mod.fuji.module.initializer.world.manager.service.structure;

import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import lombok.Value;
import net.minecraft.commands.CommandSourceStack;

@Value
public class DimensionCreationTicket {
    public CommandSourceStack source;
    public RuntimeDimensionDescriptor descriptor;
}
