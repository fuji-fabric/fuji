package io.github.sakurawald.fuji.module.initializer.command_cooldown;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
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
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.command.argument.wrapper.CommandCooldownName;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.config.model.CommandCooldownConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.service.NamedCooldownService;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.CommandCooldown;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Optional;

@Document(id = 1751826375815L, value = """
    This module allows you to define a `cooldown` for a specified `command`.
    """)
@ColorBox(id = 1751902763633L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ There are 2 types of `cooldown`.
    1. `Unnamed Cooldown`: You can use it to define a `cooldown duration` for a specified command. (It is `tested` and managed automatically)
    2. `Named Cooldown`: You have to use commands to create a `named cooldown`, and use commands to `test` it.

    <green>NOTE: If you only want to define a simple `cooling time` for a specified command, just use `unnamed cooldown`.
    """)
@ColorBox(id = 1751902885278L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Create a `named cooldown`. (With 3 seconds `cooldown duration`.)
    Issue: `/command-cooldown create example 3000`

    ◉ Test a `named cooldown`.
    Issue: `/command-cooldown test example \\\\<player\\\\> --onFailed "say false %fuji:command_cooldown_left_time example%/%fuji:command_cooldown_left_usage example%" say true`

    ◉ Reset a `named cooldown` for a player.
    Issue: `/command-cooldown reset example \\\\<player\\\\>`

    ◉ Create a `named cooldown`. (With 15 seconds `cooldown duration`, and `limit of number of use` is 3)
    Issue: `/command-cooldown create example 15000 --maxUsage 3`

    ◉ Create a global `named cooldown`.
    By default, a `named cooldown` applies `per-player`.
    A `global` named cooldown applies `per-server`.
    Issue: `/command-cooldown create example 3000 --global true`
    """)
@ColorBox(id = 1751903262817L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Make a `non-persistent named cooldown`.
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

    @Document(id = 1751826379596L, value = "Test a named-cooldown, and execute success commands or failed commands.")
    @CommandNode("test")
    private static int $test(@CommandSource ServerCommandSource source
        , @Document(id = 1751826381620L, value = "The name of a named-cooldown.") CommandCooldownName name
        , @Document(id = 1751826385172L, value = "The target player.") ServerPlayerEntity player
        , @Document(id = 1751826387810L, value = "The commands to execute if the test is failed.") Optional<StringList> onFailed
        , @Document(id = 1751826394378L, value = "The commands to execute if the test is success.") GreedyStringList onSuccess
    ) {
        ensureNamedCooldownExist(source, name);

        CommandCooldown cooldown = NamedCooldownService.getNamedCooldownList().get(name.getValue());
        StringList $onFailed = onFailed.orElse(new StringList(Collections.emptyList()));
        String key = player.getGameProfile().getName();

        /* test */
        long remainingTime = cooldown.tryUse(key, cooldown.getCooldownMs());
        int usage = cooldown.getUsage().getOrDefault(key, 0);
        int leftUsage = cooldown.getMaxUsage() - usage;
        if (remainingTime > 0 || leftUsage <= 0) {
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
        , @Document(id = 1751826403270L, value = "The name of the named-cooldown to be created.") String name
        , @Document(id = 1751826405378L, value = "How long is the cooling time ms of this named-cooldown.") long cooldownMs
        , @Document(id = 1751826407322L, value = "Max usage times of this named-cooldown. (per-player/global)") Optional<Integer> maxUsage
        , @Document(id = 1751826409664L, value = "Should we persist this named-cooldown on server shutdown.") Optional<Boolean> persistent
        , @Document(id = 1751826414070L, value = "Is this named-cooldown global or per-player.") Optional<Boolean> global) {
        ensureNamedCooldownNotExist(source, name);

        int $maxUsage = maxUsage.orElse(Integer.MAX_VALUE);
        Boolean $persistent = persistent.orElse(true);
        Boolean $global = global.orElse(false);

        CommandCooldown commandCooldown = new CommandCooldown(name, cooldownMs, $maxUsage, $persistent, $global);
        NamedCooldownService.getNamedCooldownList().put(name, commandCooldown);
        config.writeStorage();

        TextHelper.sendTextByKey(source, "command_cooldown.created", name);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826416666L, value = "Delete a named-cooldown.")
    @CommandNode("delete")
    private static int $delete(@CommandSource ServerCommandSource source, CommandCooldownName name) {
        ensureNamedCooldownExist(source, name);

        String key = name.getValue();
        NamedCooldownService.getNamedCooldownList().remove(key);
        config.writeStorage();

        TextHelper.sendTextByKey(source, "command_cooldown.deleted", name.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826418447L, value = "List all named-cooldown.")
    @CommandNode("list")
    private static int $list(@CommandSource ServerCommandSource source) {
        NamedCooldownService.getNamedCooldownList().keySet().forEach(it -> source.sendMessage(Text.literal(it)));
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826420385L, value = "Reset the timestamp of a named-cooldown for a player. (The usage times will not be reset)")
    @CommandNode("reset")
    private static int $reset(@CommandSource ServerCommandSource source
        , CommandCooldownName name
        , ServerPlayerEntity player) {
        ensureNamedCooldownExist(source, name);

        CommandCooldown commandCooldown = NamedCooldownService.getNamedCooldownList().get(name.getValue());
        config.writeStorage();

        String key = player.getGameProfile().getName();
        commandCooldown.getTimestamp().put(key, 0L);

        TextHelper.sendTextByKey(source, "command_cooldown.reset", key, name.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    private static void ensureNamedCooldownExist(ServerCommandSource source, CommandCooldownName name) {
        if (!NamedCooldownService.getNamedCooldownList().containsKey(name.getValue())) {
            TextHelper.sendTextByKey(source, "command_cooldown.not_found", name.getValue());
            throw new AbortCommandExecutionException();
        }
    }

    private static void ensureNamedCooldownNotExist(ServerCommandSource source, String name) {
        if (NamedCooldownService.getNamedCooldownList().containsKey(name)) {
            TextHelper.sendTextByKey(source, "command_cooldown.already_exists", name);
            throw new AbortCommandExecutionException();
        }
    }

    @Override
    protected void registerPlaceholder() {
        CommandCooldownPlaceholders.registerCommandCooldownLeftTimePlaceholder();
        CommandCooldownPlaceholders.registerCommandCooldownLeftTimeDatePlaceholder();
        CommandCooldownPlaceholders.registerCommandCooldownLeftUsagePlaceholder();
    }

}
