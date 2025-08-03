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
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.command.argument.wrapper.CommandCooldownName;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.config.model.CommandCooldownConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.service.NamedCooldownService;
import io.github.sakurawald.fuji.module.initializer.command_cooldown.structure.NamedCommandCooldown;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.Optional;

@Document(id = 1751826375815L, value = """
    This module allows you to define a `cooldown` for a specified `command`.
    """)
@ColorBox(id = 1751902763633L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ There are 2 types of `cooldown`.
    1. `Unnamed Cooldown`
    You can use it to define a `cooldown duration` for a specified command. (It is `tested` and managed automatically)
    A `unnamed cooldown` will not be `persisted` on the server shutdown.
    Its typical use is to define a `cooling duration` for a specified command.
    And a `unnamed cooldown` will be `tested` <green>automatically</green> when a player executes a command.
    For example: define a `3 seconds` cooling duration for `/back` command.
    To define a `unnamed cooldown`, you need to modify the config file, and issue `/fuji reload` command to apply it.

    2. `Named Cooldown`
    You have to use commands to create a `named cooldown`, and use commands to `test` it.
    A `named cooldown` will be `persisted` on the server shutdown.
    Its typical use is to define a `named cooldown`, and `associate` it with `arbitrary command instance`.
    For example, you have to use `/command-cooldown create` to `create` a `named cooldown`.
    Then, you have to use `/command-cooldown test` to `test` a `named cooldown` <green>manually</green>.
    You have to specify the `failure case commands` and `success case commands` when `test` a `named cooldown`.
    If the `conditions` defined by the `named cooldown` is satisfied, then it is a `success case`, else it is a `failure case`.
    For `success case`, we will execute `the success case command`.
    For `failure case`, we will execute `the failure case command`.

    <green>NOTE: If you only want to define a simple `cooling duration` for a specified command, just use `unnamed cooldown`.
    """)
@ColorBox(id = 1751902885278L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Create a `named cooldown`. (With 3 seconds `cooldown duration`.)
    Issue: `/command-cooldown create kitfood 3000`

    ◉ Test a `named cooldown` with `arbitrary command instance`.
    Issue: `/command-cooldown test kitfood Alice say Used successfully once`.
    This command will `test` the specified `named cooldown`:
    1. If the result is `the success case`, then it will execute `/say Used successfully once`.
    2. If the result is `the failure case`, then it will do nothing.

    Issue: `/command-cooldown test kitfood Alice --onFailed "say false" say true`
    This command will `test` the specified `named cooldown`:
    1. If the result is `the success case`, then it will execute `/say true`.
    2. If the result is `the failure case`, then it will execute `/say false`.

    <green>TIP: You can insert `%fuji:command_cooldown_left_time kitfood%` placeholder to display the remaining duration.
    <green>TIP: To specify `more than 1 command` in the `the success command` or `the failure command` place, you can use `chain` module.

    ◉ Test a `named cooldown` with `pre-defined command instance`.
    You can `pre-define` the `success case commands` and `failure case commands` in the config file.
    And use `/command-cooldown try-use kitfood Alice` to `test` it.
    This method is much more brief.

    ◉ Reset a `named cooldown` for a player.
    Issue: `/command-cooldown reset kitfood Alice`

    ◉ Create a `named cooldown`. (With 15 seconds `cooldown duration`, and `limit of number of use` is 3)
    Issue: `/command-cooldown create kitfood 15000 --maxUses 3`

    ◉ Create a global `named cooldown`.
    By default, a `named cooldown` applies `per-player`.
    A `global` named cooldown applies `per-server`.
    Issue: `/command-cooldown create kitfood 3000 --global true`
    """)
@ColorBox(id = 1751903262817L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Make a `non-persistent named cooldown`.
    By default, a `named cooldown` will be `persisted` on the `storage`.
    However, you can create a `non-persist named cooldown`.
    Issue: `/command-cooldown create kitfood 999999999999 --persistent false`
    This cooldown says that, it can be used only once after each server re-start.
    """)



@CommandNode("command-cooldown")
@CommandRequirement(level = 4)
public class CommandCooldownInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandCooldownConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandCooldownConfigModel.class) {
        @Override
        protected void beforeWriteStorage() {
            this.model().namedCooldown.list.values()
                .stream()
                .filter(it -> !it.isPersistent())
                // Reset the timestamp for non-persistent cooldown before writing storage.
                .forEach(it -> it.getTimestamp().clear());
        }
    };

    @Document(id = 1751826400837L, value = "Create a named-cooldown.")
    @CommandNode("create")
    private static int $create(@CommandSource ServerCommandSource source
        , @Document(id = 1751826403270L, value = "The name of the named-cooldown to be created.") String name
        , @Document(id = 1751826405378L, value = "How long is the cooling time ms of this named-cooldown.") long cooldownDuration
        , @Document(id = 1751826407322L, value = "The max number of uses for this named-cooldown.") Optional<Integer> maxUses
        , @Document(id = 1751826409664L, value = "Should we persist this named-cooldown on server shutdown.") Optional<Boolean> persistent
        , @Document(id = 1751826414070L, value = "Is this named-cooldown global (`per-server`) or `per-player`.") Optional<Boolean> global) {
        ensureNamedCooldownNotExist(source, name);
        int $maxUses = maxUses.orElse(Integer.MAX_VALUE);
        Boolean $persistent = persistent.orElse(true);
        Boolean $global = global.orElse(false);

        NamedCooldownService.createNamedCooldown(name, cooldownDuration, $maxUses, $persistent, $global);

        TextHelper.sendTextByKey(source, "command_cooldown.created", name);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826416666L, value = "Delete a named-cooldown.")
    @CommandNode("delete")
    private static int $delete(@CommandSource ServerCommandSource source, CommandCooldownName name) {
        ensureNamedCooldownExist(source, name);

        NamedCooldownService.deleteNamedCooldown(name);

        TextHelper.sendTextByKey(source, "command_cooldown.deleted", name.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826418447L, value = "List all named-cooldown.")
    @CommandNode("list")
    private static int $list(@CommandSource ServerCommandSource source) {
        TextHelper.sendTextByKey(source, "command_cooldown.list", NamedCooldownService.getNamedCooldownList().keySet());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826379596L, value = "Test a named-cooldown with `arbitrary command instance`, and execute `success case command` or `failure case command`.")
    @CommandNode("test")
    private static int $test(@CommandSource ServerCommandSource source
        , @Document(id = 1751826381620L, value = "The name of a named-cooldown.") CommandCooldownName name
        , @Document(id = 1751826385172L, value = "The target player.") ServerPlayerEntity player
        , @Document(id = 1751826387810L, value = "The commands to execute if the test failed.") Optional<StringList> onFailed
        , @Document(id = 1751826394378L, value = "The commands to execute if the test succeeds.") GreedyStringList onSuccess
    ) {
        ensureNamedCooldownExist(source, name);

        NamedCommandCooldown cooldown = NamedCooldownService.getNamedCooldownList().get(name.getValue());
        StringList $onFailed = onFailed.orElse(new StringList(Collections.emptyList()));

        List<String> onSuccessCommands = onSuccess.getValue();
        List<String> onFailureCommands = $onFailed.getValue();
        return NamedCooldownService.testNamedCooldown(cooldown, player, onSuccessCommands, onFailureCommands);
    }

    @Document(id = 1752917170907L, value = "Test a named-cooldown with `pre-defined command instance`, and execute `success case command` or `failure case command`.")
    @CommandNode("try-use")
    private static int $tryUse(@CommandSource ServerCommandSource source, CommandCooldownName name, ServerPlayerEntity player
    ) {
        ensureNamedCooldownExist(source, name);

        NamedCommandCooldown cooldown = NamedCooldownService.getNamedCooldownList().get(name.getValue());
        List<String> onSuccessCommands = cooldown.getTryUse().getOnSuccessCommands();
        List<String> onFailureCommands = cooldown.getTryUse().getOnFailureCommands();
        return NamedCooldownService.testNamedCooldown(cooldown, player, onSuccessCommands, onFailureCommands);
    }

    @Document(id = 1751826420385L, value = "Reset `the last use time` of a named-cooldown for a player.")
    @CommandNode("reset")
    private static int $reset(@CommandSource ServerCommandSource source
        , CommandCooldownName name
        , ServerPlayerEntity player) {
        ensureNamedCooldownExist(source, name);
        String key = NamedCommandCooldown.toKey(player);

        NamedCooldownService.resetNamedCooldownDuration(name, key);

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
    protected void registerPlaceholders() {
        CommandCooldownPlaceholders.registerCommandCooldownLeftTimePlaceholder();
        CommandCooldownPlaceholders.registerCommandCooldownLeftTimeDatePlaceholder();
        CommandCooldownPlaceholders.registerCommandCooldownLeftUsagePlaceholder();
    }

}
