package io.github.sakurawald.fuji.module.initializer.world.manager.service.structure;

import io.github.sakurawald.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import lombok.Data;

@Data
public class DimensionCreationTicket {
    public final RuntimeDimensionDescriptor descriptor;
}
