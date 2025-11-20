package mod.fuji.module.initializer.command_cooldown;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyCommandString;
import mod.fuji.core.command.argument.wrapper.impl.StringList;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.command.CommandExecutionPreEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_cooldown.config.model.CommandCooldownConfigModel;
import mod.fuji.module.initializer.command_cooldown.config.model.NamedCooldownDataModel;
import mod.fuji.module.initializer.command_cooldown.config.transformer.NamedCooldownSchemaV1Transformer;
import mod.fuji.module.initializer.command_cooldown.service.NamedCooldownService;
import mod.fuji.module.initializer.command_cooldown.service.UnnamedCooldownService;
import mod.fuji.module.initializer.command_cooldown.structure.NamedCooldownDataNode;
import mod.fuji.module.initializer.command_cooldown.structure.NamedCooldownDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826375815L, value = """
    This module allows defining a `cooldown` after command execution.
    """)
@ColorBox(id = 1751902763633L, color = ColorBox.ColorBoxTypes.NOTE, value = """
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
    You have to specify the `failure case commands` and `success case commands` when `testing` a `named cooldown`.
    If the `conditions` defined by the `named cooldown` is satisfied, then it is a `success case`, else it is a `failure case`.
    For `success case`, we will execute `the success case command`.
    For `failure case`, we will execute `the failure case command`.

    <green>TL;DR: If you only want to define a simple `cooling duration` for a specified command, just use `unnamed cooldown`.
    """)
@ColorBox(id = 1751902885278L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
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
@ColorBox(id = 1751903262817L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Make a `non-persistent named cooldown`.
    By default, a `named cooldown` will be `persisted` on the `storage`.
    However, you can create a `non-persist named cooldown`.
    Issue: `/command-cooldown create kitfood 999999999999 --persistent false`
    This cooldown says that, it can be used only once after each server re-start.
    """)


@CommandNode("command-cooldown")
@CommandRequirement(level = 4)
public class CommandCooldownInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandCooldownConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandCooldownConfigModel.class)
        .installTransformer(new NamedCooldownSchemaV1Transformer());

    public static final BaseConfigurationHandler<NamedCooldownDataModel> namedCooldownData = ObjectConfigurationHandler
        .ofModule("named-cooldown-data.json", NamedCooldownDataModel.class)
        .addPreMappingModelIntoJsonObjectHook((model) -> {
            model.getNodes()
                .stream()
                .filter(it -> it.getDescriptor() != null && !it.getDescriptor().isPersistent())
                .forEach(it -> it.getCooldown().getTimestamp().clear());
        })
        .enableAutoSaveFeature();

    @Document(id = 1751826400837L, value = "Create a named-cooldown.")
    @CommandNode("create")
    private static int $create(@CommandSource CommandSourceStack source, String name, long cooldownDuration, Optional<Integer> maxUses, Optional<Boolean> persistent, Optional<Boolean> global) {
        return NamedCooldownService
            .findNamedCooldownDescriptor(name)
            .map(it -> {
                TextHelper.sendTextByKey(source, "command_cooldown.already_exists", name);
                return CommandHelper.Return.FAILURE;
            })
            .orElseGet(() -> {
                int $maxUses = maxUses.orElse(Integer.MAX_VALUE);
                Boolean $persistent = persistent.orElse(true);
                Boolean $global = global.orElse(false);

                NamedCooldownService.createNamedCooldownDescriptor(name, cooldownDuration, $maxUses, $persistent, $global);
                TextHelper.sendTextByKey(source, "command_cooldown.created", name);
                return CommandHelper.Return.SUCCESS;
            });
    }

    @Document(id = 1751826416666L, value = "Delete a named-cooldown.")
    @CommandNode("delete")
    private static int $delete(@CommandSource CommandSourceStack source, NamedCooldownDescriptor namedCooldown, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(source, confirm, () -> {
            NamedCooldownService.deleteNamedCooldownDescriptor(namedCooldown);
            TextHelper.sendTextByKey(source, "command_cooldown.deleted", namedCooldown.getName());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1756539191422L, value = "List all `unnamed-cooldown` and `named-cooldown`.")
    @CommandNode("list")
    private static int $list(@CommandSource CommandSourceStack source) {
        TextHelper.sendTextByKey(source, "command_cooldown.list.unnamed_cooldown", UnnamedCooldownService.listUnnamedCooldowns().keySet());
        TextHelper.sendMessageByText(source, TextHelper.Formatter.formatMapMultiLine(UnnamedCooldownService.listUnnamedCooldowns()));
        TextHelper.sendMessageByText(source, TextHelper.TEXT_EMPTY);

        TextHelper.sendTextByKey(source, "command_cooldown.list.named_cooldown", NamedCooldownService.getNamedCooldownDescriptors().keySet());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826379596L, value = "Test a named-cooldown with `arbitrary command instance`, and execute `success case command` or `failure case command`.")
    @CommandNode("test")
    private static int $test(@CommandSource CommandSourceStack source
        , NamedCooldownDescriptor namedCooldown
        , ServerPlayer player
        , Optional<StringList> onFailed
        , GreedyCommandString onSuccess
    ) {
        StringList $onFailed = onFailed.orElse(new StringList(Collections.emptyList()));

        List<String> onSuccessCommands = Collections.singletonList(onSuccess.getValue());
        List<String> onFailureCommands = $onFailed.getValue();
        return NamedCooldownService.testNamedCooldown(namedCooldown, player, onSuccessCommands, onFailureCommands);
    }

    @Document(id = 1752917170907L, value = "Test a named-cooldown with `pre-defined command instance`, and execute `success case command` or `failure case command`.")
    @CommandNode("try-use")
    private static int $tryUse(@CommandSource CommandSourceStack source, NamedCooldownDescriptor namedCooldown, ServerPlayer player
    ) {
        List<String> onSuccessCommands = namedCooldown.getTryUse().getOnSuccessCommands();
        List<String> onFailureCommands = namedCooldown.getTryUse().getOnFailureCommands();
        return NamedCooldownService.testNamedCooldown(namedCooldown, player, onSuccessCommands, onFailureCommands);
    }

    @Document(id = 1751826420385L, value = "Reset `the last use time` of a named-cooldown for a player.")
    @CommandNode("reset")
    private static int $reset(@CommandSource CommandSourceStack source
        , NamedCooldownDescriptor namedCooldown
        , ServerPlayer player) {
        String key = NamedCooldownDataNode.toKey(player);

        NamedCooldownService.resetNamedCooldownDuration(namedCooldown, key);
        TextHelper.sendTextByKey(source, "command_cooldown.reset", key, namedCooldown.getName());
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void registerPlaceholders() {
        CommandCooldownPlaceholders.registerCommandCooldownLeftTimePlaceholder();
        CommandCooldownPlaceholders.registerCommandCooldownLeftTimeDatePlaceholder();
        CommandCooldownPlaceholders.registerCommandCooldownLeftUsagePlaceholder();
    }

    @TestCase(action = "Test the compatibility with other modules.", targets = {
        "Issue `/heal` command twice, the `command warmup` should be performed first, then the `command cooldown`",
        "Issue `/run as player @s heal` command twice, the command cooldown should be performed."
    })
    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority = EventConsumer.LOWER)
    private static void consumeCommandExecutionPreEvent(CommandExecutionPreEvent event) {
        if (event.getCallback().isCancelled()) return;
        CommandSourceStack commandSource = event.getCommandSource();
        if (CommandHelper.Source.isExecutedByConsole(commandSource)) return;
        if (config.model().isAdminPlayersCanBypass() && CommandHelper.Requirement.isAdmin(commandSource)) return;

        @NotNull ServerPlayer player = Objects.requireNonNull(commandSource.getPlayer());

        /* Compute the cooldown for specified command. */
        String commandString = event.getCommandString();
        long remainingDuration = UnnamedCooldownService.computeRemainingUnnamedCooldownDuration(player, commandString);
        if (remainingDuration > 0) {
            // NOTE: For unnamed cooldown type, the `second unit` is sufficient for use.
            long remainingDurationInSecond = remainingDuration / 1000;
            TextHelper.sendTextByKey(player, "command_cooldown.cooldown", remainingDurationInSecond);
            event.getCallback().cancel();
        }

    }

}
