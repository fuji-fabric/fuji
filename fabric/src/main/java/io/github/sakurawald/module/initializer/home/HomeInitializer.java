package io.github.sakurawald.module.initializer.home;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.home.command.argument.wrapper.HomeName;
import io.github.sakurawald.module.initializer.home.config.model.HomeDataModel;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class HomeInitializer extends ModuleInitializer {

    @Getter
    private static final BaseConfigurationHandler<HomeDataModel> storage = new ObjectConfigurationHandler<>("home.json", HomeDataModel.class)
        .autoSaveEveryMinute();

    public static Map<String, SpatialPose> withHomes(@NotNull ServerPlayerEntity player) {
        String playerName = player.getGameProfile().getName();
        Map<String, Map<String, SpatialPose>> homes = storage.model().name2home;
        homes.computeIfAbsent(playerName, k -> new HashMap<>());
        return homes.get(playerName);
    }

    @CommandNode("home tp")
    private static int $tp(@CommandSource ServerPlayerEntity player, HomeName home) {
        Map<String, SpatialPose> homes = withHomes(player);
        String homeName = home.getValue();
        ensureHomeExists(player, homes, homeName);

        SpatialPose spatialPose = homes.get(homeName);
        spatialPose.teleport(player);
        return CommandHelper.Return.SUCCESS;
    }

    private static void ensureHomeExists(ServerPlayerEntity player, Map<String, SpatialPose> homes, String homeName) {
        if (!homes.containsKey(homeName)) {
            TextHelper.sendMessageByKey(player, "home.not_found", homeName);
            throw new AbortCommandExecutionException();
        }
    }

    @CommandNode("home unset")
    private static int $unset(@CommandSource ServerPlayerEntity player, HomeName home) {
        Map<String, SpatialPose> homes = withHomes(player);
        String homeName = home.getValue();
        ensureHomeExists(player, homes, homeName);

        homes.remove(homeName);
        TextHelper.sendMessageByKey(player, "home.unset.success", homeName);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("home set")
    private static int $set(@CommandSource ServerPlayerEntity player, HomeName home, Optional<Boolean> override) {
        Map<String, SpatialPose> name2position = withHomes(player);
        String homeName = home.getValue();

        if (name2position.containsKey(homeName)) {
            if (!override.orElse(false)) {
                TextHelper.sendMessageByKey(player, "home.set.fail.need_override", homeName);
                return CommandHelper.Return.FAIL;
            }
        }

        Optional<Integer> limit = PermissionHelper.getMeta(player.getUuid(), "fuji.home.home_limit", Integer::valueOf);
        if (limit.isPresent() && name2position.size() >= limit.get()) {
            TextHelper.sendMessageByKey(player, "home.set.fail.limit");
            return CommandHelper.Return.FAIL;
        }

        name2position.put(homeName, SpatialPose.of(player));
        TextHelper.sendMessageByKey(player, "home.set.success", homeName);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("home list")
    private static int $list(@CommandSource ServerPlayerEntity player) {
        TextHelper.sendMessageByKey(player, "home.list", withHomes(player).keySet());
        return CommandHelper.Return.SUCCESS;
    }

}
