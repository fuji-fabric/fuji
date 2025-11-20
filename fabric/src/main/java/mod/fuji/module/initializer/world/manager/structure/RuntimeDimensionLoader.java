package mod.fuji.module.initializer.world.manager.structure;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.FabricApiHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.extension.SimpleRegistryExtension;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.MappedRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.NotNull;

#if MC_VER <= MC_1_20_4
import com.mojang.serialization.Lifecycle;
#elif MC_VER > MC_1_20_4
import net.minecraft.core.RegistrationInfo;
#endif

public class RuntimeDimensionLoader {

    private static
    #if MC_VER <= MC_1_20_4
    Lifecycle
    #elif MC_VER > MC_1_20_4
    RegistrationInfo
    #endif
    makeDefaultRegistryEntryInfo() {
        #if MC_VER <= MC_1_20_4
        return Lifecycle.stable();
        #elif MC_VER > MC_1_20_4
        return RegistrationInfo.BUILT_IN;
        #endif
    }

    public static void loadRuntimeDimension(ServerLevel dimension, LevelStem dimensionOptions) {
        MappedRegistry<LevelStem> dimensionOptionsRegistry = (MappedRegistry<LevelStem>) RegistryHelper.getRegistry(Registries.LEVEL_STEM);
        boolean original = ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$isFrozen();
        ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$setFrozen(false);

        ResourceKey<Level> worldRegistryKey = dimension.dimension();
        ResourceKey<LevelStem> dimensionOptionsRegistryKey = Registries.levelToLevelStem(worldRegistryKey);


        if (!dimensionOptionsRegistry.containsKey(dimensionOptionsRegistryKey)) {
            LogUtil.debug("Add entry into Registry<DimensionOptions>: key = {}, value = {}", dimensionOptionsRegistryKey, dimensionOptions);
            dimensionOptionsRegistry.register(dimensionOptionsRegistryKey, dimensionOptions, makeDefaultRegistryEntryInfo());
        }
        ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$setFrozen(original);

        MinecraftServer server = ServerHelper.getServer();
        server.levels.put(dimension.dimension(), dimension);

        try {
            FabricApiHelper.fireOnWorldLoadEvent(server, dimension);
        } catch (Exception e) {
            LogUtil.error("Failed to fire onWorldLoad event in fabric-api mod.", e);
        }
    }

    public static void unloadDimension(@NotNull ServerLevel world) {
        MinecraftServer server = world.getServer();
        ResourceKey<Level> dimensionKey = world.dimension();
        if (server.levels.remove(dimensionKey, world)) {
            /* Fire an unload event */
            try {
                FabricApiHelper.fireOnWorldUnloadEvent(server,world);
            } catch (Exception e) {
                LogUtil.error("Failed to fire onWorldUnload event in fabric-api mod.", e);
            }

            /* Remove the entry from registry. */
            MappedRegistry<LevelStem> dimensionsRegistry = (MappedRegistry<LevelStem>) RegistryHelper.getRegistry(Registries.LEVEL_STEM);
            SimpleRegistryExtension.remove(dimensionsRegistry, dimensionKey.location());
        }
    }
}
