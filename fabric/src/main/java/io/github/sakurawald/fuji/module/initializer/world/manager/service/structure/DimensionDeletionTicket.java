package io.github.sakurawald.fuji.module.initializer.world.manager.service.structure;

import lombok.Data;
import net.minecraft.server.world.ServerWorld;

@Data
public class DimensionDeletionTicket {

    public final ServerWorld world;
    public final boolean deleteWorldFiles;

}
