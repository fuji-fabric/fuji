package mod.fuji.module.initializer.world.manager.structure;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.FabricApiHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.extension.SimpleRegistryExtension;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import org.jetbrains.annotations.NotNull;

#if MC_VER <= MC_1_20_4
import com.mojang.serialization.Lifecycle;
#elif MC_VER > MC_1_20_4
import net.minecraft.registry.entry.RegistryEntryInfo;
#endif

public class RuntimeDimensionLoader {

    private static
    #if MC_VER <= MC_1_20_4
    Lifecycle
    #elif MC_VER > MC_1_20_4
    RegistryEntryInfo
    #endif
    makeDefaultRegistryEntryInfo() {
        #if MC_VER <= MC_1_20_4
        return Lifecycle.stable();
        #elif MC_VER > MC_1_20_4
        return RegistryEntryInfo.DEFAULT;
        #endif
    }

    public static void loadRuntimeDimension(ServerWorld dimension, DimensionOptions dimensionOptions) {
        SimpleRegistry<DimensionOptions> dimensionOptionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.getRegistry(RegistryKeys.DIMENSION);
        boolean original = ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$isFrozen();
        ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$setFrozen(false);

        RegistryKey<World> worldRegistryKey = dimension.getRegistryKey();
        RegistryKey<DimensionOptions> dimensionOptionsRegistryKey = RegistryKeys.toDimensionKey(worldRegistryKey);


        if (!dimensionOptionsRegistry.contains(dimensionOptionsRegistryKey)) {
            LogUtil.debug("Add entry into Registry<DimensionOptions>: key = {}, value = {}", dimensionOptionsRegistryKey, dimensionOptions);
            dimensionOptionsRegistry.add(dimensionOptionsRegistryKey, dimensionOptions, makeDefaultRegistryEntryInfo());
        }
        ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$setFrozen(original);

        MinecraftServer server = ServerHelper.getServer();
        server.worlds.put(dimension.getRegistryKey(), dimension);

        try {
            FabricApiHelper.fireOnWorldLoadEvent(server, dimension);
        } catch (Exception e) {
            LogUtil.error("Failed to fire onWorldLoad event in fabric-api mod.", e);
        }
    }

    public static void unloadDimension(@NotNull ServerWorld world) {
        MinecraftServer server = world.getServer();
        RegistryKey<World> dimensionKey = world.getRegistryKey();
        if (server.worlds.remove(dimensionKey, world)) {
            /* Fire an unload event */
            try {
                FabricApiHelper.fireOnWorldUnloadEvent(server,world);
            } catch (Exception e) {
                LogUtil.error("Failed to fire onWorldUnload event in fabric-api mod.", e);
            }

            /* Remove the entry from registry. */
            SimpleRegistry<DimensionOptions> dimensionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.getRegistry(RegistryKeys.DIMENSION);
            SimpleRegistryExtension.remove(dimensionsRegistry, dimensionKey.getValue());
        }
    }
}
