package io.github.sakurawald.fuji.module.initializer.command_state.service;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.manager.impl.cache.structure.Cache;
import io.github.sakurawald.fuji.module.initializer.command_state.CommandStateInitializer;
import io.github.sakurawald.fuji.module.initializer.command_state.structure.PlayerStates;
import io.github.sakurawald.fuji.module.initializer.command_state.structure.StateDescriptor;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class CommandStateService {

    public static @NotNull List<StateDescriptor> listStateDescriptors() {
        return CommandStateInitializer.config.model()
            .getStateDescriptors()
            .stream()
            .filter(StateDescriptor::isEnable)
            .filter(it -> {
                boolean flag = it.getDefinition().getPredicateCommands().isEmpty();
                if (flag) {
                    LogUtil.warn("Ignore the '{}' state descriptor. (Reason: empty definition)", it.getId());
                }
                return !flag;
            })
            .toList();
    }

    public static @NotNull List<String> listStateIds() {
        return listStateDescriptors()
            .stream()
            .map(StateDescriptor::getId)
            .toList();
    }

    public static Optional<StateDescriptor> findStateDescriptor(@NotNull String id) {
        return listStateDescriptors()
            .stream()
            .filter(it -> it.getId().equals(id))
            .findFirst();
    }

    public static void updateAllCommandStates() {
        listStateDescriptors().forEach(CommandStateService::updateCommandState);
    }

    public static void updateCommandState(@NotNull StateDescriptor stateDescriptor) {
        LogUtil.disabled("Update the command state '{}' for online players.", stateDescriptor.getId());
        PlayerHelper.Lookup
            .getOnlinePlayers()
            .forEach(player -> updateCommandState(player, stateDescriptor));
    }

    public static void withPlayerStateMap(@NotNull ServerPlayerEntity player, @NotNull Consumer<PlayerStates> consumer) {
        String playerName = PlayerHelper.getPlayerName(player);

        PlayerStates playerStates = CommandStateInitializer.data.model()
            .getPlayerStatesMap()
            .computeIfAbsent(playerName, key -> new PlayerStates());
        consumer.accept(playerStates);
    }

    private static void executeCommandStateEventCommands(@NotNull ServerPlayerEntity player, @NotNull StateDescriptor stateDescriptor, boolean isEnterEvent) {
        List<String> commands;
        if (isEnterEvent) {
            commands = stateDescriptor.getEvents().getOnEnterThisStateCommands();
        } else {
            commands = stateDescriptor.getEvents().getOnLeaveThisStateCommands();
        }

        ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(player.getCommandSource());
        CommandExecutor.executeBatch(extendedCommandSource, commands);
    }

    private static void updateCommandState(@NotNull ServerPlayerEntity player, @NotNull StateDescriptor stateDescriptor) {
        String stateId = stateDescriptor.getId();

        withPlayerStateMap(player, playerStates -> {
            playerStates
                .getStateMap()
                .compute(stateId, (key, stateCache) -> {
                    /* Check cached value. */
                    if (stateCache != null
                        && !stateCache.isWithinExpirationDuration(Duration.ofSeconds(stateDescriptor.getUpdateIntervalSeconds()))) {
                        return stateCache;
                    }

                    /* Execute predicate commands. */
                    boolean currentStateStatus = checkCurrentStateValue(player, stateDescriptor);

                    /* Execute event commands. */
                    if (stateCache == null
                        || currentStateStatus != stateCache.getValue()) {
                        executeCommandStateEventCommands(player, stateDescriptor, currentStateStatus);
                    }

                    /* Update the value. */
                    return Cache.of(currentStateStatus);
                });
        });
    }

    public static boolean checkCurrentStateValue(@NotNull ServerPlayerEntity player, @NotNull StateDescriptor stateDescriptor) {
        /* Execute the predicate commands, to get the return values. */
        ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(player.getCommandSource());
        List<String> predicateCommands = stateDescriptor.getDefinition().getPredicateCommands();
        List<Integer> commandReturnValues = CommandExecutor.executeBatch(extendedCommandSource, predicateCommands);

        /* Map the return values into boolean value. */
        return commandReturnValues
            .stream()
            .allMatch(CommandHelper.Return::isSuccess);
    }

}
