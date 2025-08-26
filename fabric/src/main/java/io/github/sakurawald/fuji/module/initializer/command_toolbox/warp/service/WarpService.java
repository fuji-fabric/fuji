package io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.WarpInitializer;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.structure.WarpDescriptor;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpService {

    public static Optional<WarpDescriptor> findWarp(@NotNull String warpId) {
        return Optional
            .ofNullable(WarpInitializer.data.model().warps.get(warpId))
            .map(value -> {
                value.setKey(warpId);
                return value;
            });
    }

    public static void doWarp(@NotNull WarpDescriptor warpDescriptor, @NotNull ServerPlayerEntity player) {
        warpDescriptor.getPosition().teleport(player);

        ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(player.getCommandSource());
        List<String> commandList = warpDescriptor.getEvent().getOnWarped().getCommandList();
        CommandExecutor.executeBatch(extendedCommandSource, commandList);
        TextHelper.sendTextByKey(player,"warp.tp.success", warpDescriptor.getDisplayName());
    }

    public static @NotNull Optional<WarpDescriptor> deleteWarp(@NotNull WarpDescriptor warpDescriptor) {
        String id = warpDescriptor.getKey();
        @Nullable WarpDescriptor previousValue = WarpInitializer.data.model().warps.remove(id);
        return Optional.ofNullable(previousValue);
    }

    public static @NotNull List<WarpDescriptor> listWarps() {
        return WarpInitializer.data.model().warps.values().stream().toList();
    }

    public static @NotNull List<String> listWarpIds() {
        return WarpInitializer.data.model().warps.keySet().stream().toList();
    }

    public static void createWarp(@NotNull ServerPlayerEntity player, @NotNull String warpId) {
        WarpDescriptor newValue = new WarpDescriptor(GlobalPos.of(player)).withDisplayName(warpId);
        WarpInitializer.data.model().warps.put(warpId, newValue);
    }

}
