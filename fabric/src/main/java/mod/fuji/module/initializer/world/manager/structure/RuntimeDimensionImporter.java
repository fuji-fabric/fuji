package mod.fuji.module.initializer.world.manager.structure;

import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.module.initializer.world.manager.WorldInitializer;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.ChunkGeneratorType;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.WorldPresetType;
import java.nio.file.Path;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

public class RuntimeDimensionImporter {

    public static @NotNull RuntimeDimensionDescriptor importRuntimeDimensionDescriptor(ResourceLocation dimensionIdentifier, WorldPresetType worldPresetType, ResourceLocation dimensionTypeIdentifier, ChunkGeneratorType chunkGeneratorType, String chunkGeneratorParameter, long seed) {
        /* Make the descriptor. */
        RuntimeDimensionDescriptor runtimeDimensionDescriptor = new RuntimeDimensionDescriptor();
        runtimeDimensionDescriptor.dimension = dimensionIdentifier.toString();
        runtimeDimensionDescriptor.seed = seed;
        if (worldPresetType != null) {
            runtimeDimensionDescriptor.worldPresetType = worldPresetType;
            runtimeDimensionDescriptor.dimension_type = BuiltinDimensionTypes.OVERWORLD_EFFECTS.toString();
        } else {
            runtimeDimensionDescriptor.dimension_type = dimensionTypeIdentifier.toString();
            runtimeDimensionDescriptor.chunkGeneratorType = chunkGeneratorType;
            runtimeDimensionDescriptor.chunkGeneratorParameters = chunkGeneratorParameter;
        }
        runtimeDimensionDescriptor.setShouldTickTime(true);

        /* Save the descriptor. */
        WorldInitializer.world.model().dimension_list.add(runtimeDimensionDescriptor);
        WorldInitializer.world.writeStorage();
        return runtimeDimensionDescriptor;
    }

    public static Path getLevelSavePath() {
        ResourceKey<Level> dimensionKey = ServerHelper.getServer().overworld().dimension();
        LevelStorageSource.LevelStorageAccess session = ServerHelper.getServer().storageSource;
        return session.getDimensionPath(dimensionKey);
    }
}
