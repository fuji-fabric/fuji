package io.github.sakurawald.fuji.module.initializer.command_cooldown;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyStringList;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.StringList;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.structure.Cooldown;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.command.argument.wrapper.CommandCooldownName;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.config.model.CommandCooldownConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.CommandCooldown;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Document(id = 1751826375815L, value = """
    This module allows you to define a `cooldown` for specified commands.
    """)

@ColorBox(id = 1751902763633L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ There are 2 types of `cooldown`.
    1. `Unnamed Cooldown`: You can use it to define a cooldown time for a specified command. (The cooldown is tested and managed automatically)
    2. `Named Cooldown`: You have to use commands to create `named cooldown`, and use commands to `test it`.

    NOTE: If you only want to define a `cooldown` for some commands, just use `unnamed cooldown`.
    """)
@ColorBox(id = 1751902885278L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Create a named cooldown. (With 3 seconds cooldown)
    Issue: `/command-cooldown create example 3000`
    """)
@ColorBox(id = 1751903050083L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Test a named cooldown.
    Issue: `/command-cooldown test example \\<player\\> --onFailed "say false %fuji:command_cooldown_left_time example%/%fuji:command_cooldown_left_usage example%" say true`
    """)
@ColorBox(id = 1751903086020L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Reset a named cooldown for a player.
    Issue: `/command-cooldown reset example \\<player\\>`
    """)
@ColorBox(id = 1751903125621L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Create a named cooldown. (With 15 seconds cooldown, and 3 max usage times)
    Issue: `/command-cooldown create example 15000 --maxUsage 3`
    """)
@ColorBox(id = 1751903203558L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Create a named cooldown. (Global, all players shares the same cooldown instance)
    Issue: `/command-cooldown create example 3000 --global true`
    """)
@ColorBox(id = 1751903262817L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    By default, a `named cooldown` will be `persisted` on the `storage`.
    However, you can create a `non-persist named cooldown`.
    Issue: `/command-cooldown create example 999999999999 --persistent false`
    This cooldown says that, it can be used only once after each server re-start.
    """)


@CommandNode("command-cooldown")
@CommandRequirement(level = 4)
public class CommandCooldownInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandCooldownConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandCooldownConfigModel.class) {
        @Override
        public void beforeWriteStorage() {
            this.model().namedCooldown.list.values()
                .stream()
                .filter(it -> !it.isPersistent())
                // Reset the timestamp for non-persistent cooldown before writing storage.
                .forEach(it -> it.getTimestamp().clear());
        }
    };

    private static final MutableText NOT_COOLDOWN_FOUND_ERROR_TEXT = Text.literal("NOT_COOLDOWN_FOUND_ERROR");

    private static final Map<String, Cooldown<String>> player2cooldown = new HashMap<>();

    public static long computeCooldown(ServerPlayerEntity player, @NotNull String commandLine) {
        String playerName = PlayerHelper.getPlayerName(player);
        Cooldown<String> cooldown = player2cooldown.computeIfAbsent(playerName, k -> new Cooldown<>());

        Optional<Map.Entry<String, Long>> first = config.model().unnamed_cooldown.entrySet()
            .stream()
            .filter(it -> commandLine.matches(it.getKey()))
            .findFirst();
        return first.map(entry -> cooldown.tryUse(entry.getKey(), entry.getValue()))
            .orElse(-1L);
    }

    @Document(id = 1751826379596L, value = "Test a named-cooldown, and execute success commands or failed commands.")
    @CommandNode("test")
    private static int $test(@CommandSource ServerCommandSource source
        , @Document(id = 1751826381620L, value = "The named-cooldown.") CommandCooldownName name
        , @Document(id = 1751826385172L, value = "The target player.") ServerPlayerEntity player
        , @Document(id = 1751826387810L, value = "The commands to execute if the test is failed.") Optional<StringList> onFailed
        , @Document(id = 1751826394378L, value = "The commands to execute if the test is success.") GreedyStringList onSuccess
    ) {
        ensureExist(source, name);

        CommandCooldown cooldown = config.model().namedCooldown.list.get(name.getValue());
        StringList $onFailed = onFailed.orElse(new StringList(Collections.emptyList()));
        String key = player.getGameProfile().getName();

        /* test */
        long leftTime = cooldown.tryUse(key, cooldown.getCooldownMs());
        int usage = cooldown.getUsage().getOrDefault(key, 0);
        int leftUsage = cooldown.getMaxUsage() - usage;
        if (leftTime > 0 || leftUsage <= 0) {
            CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), $onFailed.getValue());
            return CommandHelper.Return.FAIL;
        }

        cooldown.getUsage().compute(key, (k, v) -> v == null ? 1 : v + 1);
        config.writeStorage();
        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), onSuccess.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826400837L, value = "Create a named-cooldown.")
    @CommandNode("create")
    private static int $create(@CommandSource ServerCommandSource source
        , @Document(id = 1751826403270L, value = "The name for named-cooldown.") String name
        , @Document(id = 1751826405378L, value = "How long is the cooling time ms of this named-cooldown.") long cooldownMs
        , @Document(id = 1751826407322L, value = "Max usage times of this named-cooldown. (per-player/global)") Optional<Integer> maxUsage
        , @Document(id = 1751826409664L, value = "Should we persist this named-cooldown on server shutdown.") Optional<Boolean> persistent
        , @Document(id = 1751826414070L, value = "Is this named-cooldown global or per-player.") Optional<Boolean> global) {
        ensureNotExist(source, name);

        int $maxUsage = maxUsage.orElse(Integer.MAX_VALUE);
        Boolean $persistent = persistent.orElse(true);
        Boolean $global = global.orElse(false);

        CommandCooldown commandCooldown = new CommandCooldown(name, cooldownMs, $maxUsage, $persistent, $global);
        config.model().namedCooldown.list.put(name, commandCooldown);
        config.writeStorage();

        TextHelper.sendTextByKey(source, "command_cooldown.created", name);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826416666L, value = "Delete a named-cooldown.")
    @CommandNode("delete")
    private static int $delete(@CommandSource ServerCommandSource source, CommandCooldownName name) {
        ensureExist(source, name);

        String key = name.getValue();
        config.model().namedCooldown.list.remove(key);
        config.writeStorage();

        TextHelper.sendTextByKey(source, "command_cooldown.deleted", name.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826418447L, value = "List all named-cooldown.")
    @CommandNode("list")
    private static int $list(@CommandSource ServerCommandSource source) {
        config.model().namedCooldown.list.keySet().forEach(it -> source.sendMessage(Text.literal(it)));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826420385L, value = "Reset the timestamp of a named-cooldown for a player. (The usage times will not be reset)")
    @CommandNode("reset")
    private static int $reset(@CommandSource ServerCommandSource source
        , CommandCooldownName name
        , ServerPlayerEntity player) {
        ensureExist(source, name);

        CommandCooldown commandCooldown = config.model().namedCooldown.list.get(name.getValue());
        config.writeStorage();

        String key = player.getGameProfile().getName();
        commandCooldown.getTimestamp().put(key, 0L);

        TextHelper.sendTextByKey(source, "command_cooldown.reset", key, name.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    private static void ensureExist(ServerCommandSource source, CommandCooldownName name) {
        if (!config.model().namedCooldown.list.containsKey(name.getValue())) {
            TextHelper.sendTextByKey(source, "command_cooldown.not_found", name.getValue());
            throw new AbortCommandExecutionException();
        }
    }

    private static void ensureNotExist(ServerCommandSource source, String name) {
        if (config.model().namedCooldown.list.containsKey(name)) {
            TextHelper.sendTextByKey(source, "command_cooldown.already_exists", name);
            throw new AbortCommandExecutionException();
        }
    }


    @DocStringProvider(id = 1751999769680L, value = """
        Returns the `left time` for `specified named cooldown` in mill-seconds.
        """)
    @DocStringProvider(id = 1751999791863L, value = """
        Returns the `left usage times` for `specified named cooldown` in integer.
        """)
    @Override
    protected void registerPlaceholder() {
        PlaceholderDescriptor leftTimeDescriptor = new PlaceholderDescriptor("command_cooldown_left_time", 1751999769680L);
        PlaceholderHelper.registerPlayerPlaceholder(leftTimeDescriptor, (player, args) -> {
            CommandCooldown cooldown = config.model().namedCooldown.list.get(args);
            if (cooldown == null) return NOT_COOLDOWN_FOUND_ERROR_TEXT;

            String key = player.getGameProfile().getName();
            long leftTime = cooldown.getCooldown(key, cooldown.getCooldownMs());
            leftTime = Math.max(0, leftTime);
            return Text.literal(String.valueOf(leftTime));
        });

        PlaceholderDescriptor leftUsageDescriptor = new PlaceholderDescriptor("command_cooldown_left_usage", 1751999791863L);
        PlaceholderHelper.registerPlayerPlaceholder(leftUsageDescriptor, (player, args) -> {
            CommandCooldown cooldown = config.model().namedCooldown.list.get(args);
            if (cooldown == null) return NOT_COOLDOWN_FOUND_ERROR_TEXT;

            String key = player.getGameProfile().getName();
            int usage = cooldown.getUsage().getOrDefault(key, 0);
            int leftUsage = cooldown.getMaxUsage() - usage;
            return Text.literal(String.valueOf(leftUsage));
        });
    }

}
