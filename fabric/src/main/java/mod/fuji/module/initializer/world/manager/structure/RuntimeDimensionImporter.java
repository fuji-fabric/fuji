package mod.fuji.module.initializer.world.manager.structure;

import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.module.initializer.world.manager.WorldInitializer;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.ChunkGeneratorType;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.WorldPresetType;
import java.nio.file.Path;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;

public class RuntimeDimensionImporter {

    public static @NotNull RuntimeDimensionDescriptor importRuntimeDimensionDescriptor(Identifier dimensionIdentifier, WorldPresetType worldPresetType, Identifier dimensionTypeIdentifier, ChunkGeneratorType chunkGeneratorType, String chunkGeneratorParameter, long seed) {
        /* Make the descriptor. */
        RuntimeDimensionDescriptor runtimeDimensionDescriptor = new RuntimeDimensionDescriptor();
        runtimeDimensionDescriptor.dimension = dimensionIdentifier.toString();
        runtimeDimensionDescriptor.seed = seed;
        if (worldPresetType != null) {
            runtimeDimensionDescriptor.worldPresetType = worldPresetType;
            runtimeDimensionDescriptor.dimension_type = DimensionTypes.OVERWORLD_ID.toString();
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
        RegistryKey<World> dimensionKey = ServerHelper.getServer().getOverworld().getRegistryKey();
        LevelStorage.Session session = ServerHelper.getServer().session;
        return session.getWorldDirectory(dimensionKey);
    }
}
