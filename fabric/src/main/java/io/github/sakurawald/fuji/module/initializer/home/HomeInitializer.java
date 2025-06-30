package io.github.sakurawald.fuji.module.initializer.home;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.home.command.argument.wrapper.HomeName;
import io.github.sakurawald.fuji.module.initializer.home.config.model.HomeDataModel;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Document("""
    Allows players to define its home.
    """)
public class HomeInitializer extends ModuleInitializer {

    private static final MetaDescriptor<Integer> MAX_HOME_AMOUNT_META = new MetaDescriptor<>("fuji.home.home_limit", Integer::valueOf, """
        The home amount limit for this player.
        """);

    @Getter
    private static final BaseConfigurationHandler<HomeDataModel> storage = new ObjectConfigurationHandler<>("home.json", HomeDataModel.class)
        .setAutoSaveEveryMinute();

    public static Map<String, GlobalPos> withHomes(@NotNull ServerPlayerEntity player) {
        String playerName = player.getGameProfile().getName();
        Map<String, Map<String, GlobalPos>> homes = storage.model().name2home;
        homes.computeIfAbsent(playerName, k -> new HashMap<>());
        return homes.get(playerName);
    }

    @CommandNode("home tp")
    private static int $tp(@CommandSource ServerPlayerEntity player, HomeName home) {
        Map<String, GlobalPos> homes = withHomes(player);
        String homeName = home.getValue();
        ensureHomeExists(player, homes, homeName);

        GlobalPos globalPos = homes.get(homeName);
        globalPos.teleport(player);
        return CommandHelper.Return.SUCCESS;
    }

    private static void ensureHomeExists(ServerPlayerEntity player, Map<String, GlobalPos> homes, String homeName) {
        if (!homes.containsKey(homeName)) {
            TextHelper.sendMessageByKey(player, "home.not_found", homeName);
            throw new AbortCommandExecutionException();
        }
    }

    @CommandNode("home unset")
    private static int $unset(@CommandSource ServerPlayerEntity player, HomeName home) {
        Map<String, GlobalPos> homes = withHomes(player);
        String homeName = home.getValue();
        ensureHomeExists(player, homes, homeName);

        homes.remove(homeName);
        TextHelper.sendMessageByKey(player, "home.unset.success", homeName);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("home set")
    private static int $set(@CommandSource ServerPlayerEntity player, HomeName home, Optional<Boolean> override) {
        Map<String, GlobalPos> name2position = withHomes(player);
        String homeName = home.getValue();

        if (name2position.containsKey(homeName)) {
            if (!override.orElse(false)) {
                TextHelper.sendMessageByKey(player, "home.set.fail.need_override", homeName);
                return CommandHelper.Return.FAIL;
            }
        }

        Optional<Integer> limit = LuckpermsHelper.getMeta(player.getUuid(), MAX_HOME_AMOUNT_META);
        if (limit.isPresent() && name2position.size() >= limit.get()) {
            TextHelper.sendMessageByKey(player, "home.set.fail.limit");
            return CommandHelper.Return.FAIL;
        }

        name2position.put(homeName, GlobalPos.of(player));
        TextHelper.sendMessageByKey(player, "home.set.success", homeName);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("home list")
    private static int $list(@CommandSource ServerPlayerEntity player) {
        TextHelper.sendMessageByKey(player, "home.list", withHomes(player).keySet());
        return CommandHelper.Return.SUCCESS;
    }

}
