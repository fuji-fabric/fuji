package io.github.sakurawald.fuji.module.initializer.world.manager.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.event.impl.ServerWorldEvents;
import io.github.sakurawald.fuji.core.extension.SimpleRegistryExtension;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;

#if MC_VER <= MC_1_20_4
import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.NotNull;
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
        SimpleRegistry<DimensionOptions> dimensionOptionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
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
        ServerWorldEvents.LOAD.invoker().fire(server, dimension);
    }

    public static void unloadDimension(@NotNull ServerWorld world) {
        // FIXME: Use the vanilla function to handle dimension shutdown.
        MinecraftServer server = world.getServer();
        RegistryKey<World> dimensionKey = world.getRegistryKey();
        if (server.worlds.remove(dimensionKey, world)) {
            /* Fire an unload event */
            ServerWorldEvents.UNLOAD.invoker().fire(server, world);

            /* Remove the entry from registry. */
            SimpleRegistry<DimensionOptions> dimensionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
            SimpleRegistryExtension.remove(dimensionsRegistry, dimensionKey.getValue());
        }
    }
}
