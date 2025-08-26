package io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.WarpInitializer;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.command.argument.wrapper.WarpName;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.structure.WarpDescriptor;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpService {

    public static Optional<WarpDescriptor> findWarp(@NotNull String warpName) {
        return Optional.ofNullable(WarpInitializer.data.model().warps.get(warpName));
    }

    public static int withWarp(@NotNull WarpName warpName, @NotNull Function<WarpDescriptor, Integer> consumer) {
        return findWarp(warpName.getValue())
            .map(consumer)
            .orElse(CommandHelper.Return.FAILURE);
    }

    public static void doWarp(@NotNull WarpDescriptor warpDescriptor, @NotNull ServerPlayerEntity player) {
        warpDescriptor.getPosition().teleport(player);

        ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(player.getCommandSource());
        List<String> commandList = warpDescriptor.getEvent().on_warped.command_list;
        CommandExecutor.executeBatch(extendedCommandSource, commandList);
        TextHelper.sendTextByKey(player,"warp.tp.success", warpDescriptor.displayName);
    }

    public static @NotNull Optional<WarpDescriptor> deleteWarp(@NotNull String warpName) {
        @Nullable WarpDescriptor previousValue = WarpInitializer.data.model().warps.remove(warpName);
        return Optional.ofNullable(previousValue);
    }

    public static @NotNull List<WarpDescriptor> listWarps() {
        return WarpInitializer.data.model().warps.values().stream().toList();
    }

    public static void createWarp(@NotNull ServerPlayerEntity player, @NotNull String warpName) {
        WarpDescriptor newValue = new WarpDescriptor(GlobalPos.of(player)).withDisplayName(warpName);
        WarpInitializer.data.model().warps.put(warpName, newValue);
    }

    public static @NotNull List<String> listWarpIds() {
        return WarpInitializer.data.model().warps.keySet().stream().toList();
    }
}
