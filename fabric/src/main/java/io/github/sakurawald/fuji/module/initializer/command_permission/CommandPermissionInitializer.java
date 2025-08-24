package io.github.sakurawald.fuji.module.initializer.command_permission;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.impl.CommandEvents;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_permission.config.model.CommandPermissionConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_permission.gui.CommandPermissionGui;
import io.github.sakurawald.fuji.module.initializer.command_permission.structure.CommandNodePermissionWrapper;
import io.github.sakurawald.fuji.module.initializer.command_permission.structure.CommandPermissionRule;
import io.github.sakurawald.fuji.module.initializer.command_permission.structure.WrappedPredicate;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Cite("https://github.com/DrexHD/VanillaPermissions")
@Document(id = 1751826772214L, value = """
    This module provides the `luckperms permissions` for `all commands`.
    """)
@ColorBox(id = 1751970566759L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    The vanilla Minecraft use a command system, named `brigadier command system`.
    All `commands` are `registered`, `parsed` and `executed` by this system.
    In the design of this command system, all commands are built into a `tree structure`.
    That is the `command tree`.
    All commands starts with the `root command`, and walking down the `path` of the `command tree`.
    All commands are a `direct child` or `in-direct child` of the `root command node`.
    Based on these facts, we can `identify` a `command node` using its `path` in the `command tree`.

    For example, the command string `/gamemode creative Steve`.
    Can be parsed into command nodes: `gamemode`, `creative` and `Steve`.
    And its `command path` is `gamemode.gamemode.target`.

    You can issue `/command-permission describe gamemode creative Steve` to see what is happening.
    To see the `tree structure` of this command node, issue `/help gamemode` command.

    To open the command permission GUI, issue `/command-permission gui` command.
    To list all commands and their command path, issue `/fuji inspect server-commands` command.
    """)
@ColorBox(id = 1751970931219L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    Let's continue, the `brigadier command system`, organize all `command nodes` into a `tree structure`.
    And for each `command node`, there is a `requirement` option, it's a `predicate`, to decide whether the `command source` can use this `command node`.

    That's the core part.
    The `command_permission` module, will walk down the entire `command tree`.
    And `wrap` the `requirement` option for each `command node`.
    And then, we can assign a `luckperms permission` for each `command node`.
    That's because, we can `identify` a `command node` using its `command path` in the `command tree`.

    Issue: `/command-permission describe gamemode creative Steve` to see how it works.
    """)
@ColorBox(id = 1751971202478L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How does `command_permission` module handles the `inheritance permission`, `wildcard permission` and `regex permission`?

    Actually, the `command_permission` module didn't handle them.
    The module only does one simple thing: Let's check if the command source has the corresponding luckperms permission `fuji.permission.\\<command path of that target command\\>`.
    The complex things like `inheritance permission`, `wildcard permission` and `regex permission` are all processed by `luckperms` mod.
    Yeah, the `luckperms` mod does the complex `permission calculation`.
    """)
@ColorBox(id = 1751971384898L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Allow everyone to use `/gamemode` command.
    You can issue `/lp group default permission set fuji.permission.gamemode true`

    NOTE: If you want to allow the client-side to use the gamemode switcher menu, you have to install extra mods in the client-side, to let the client-side believe they are `op`, and they can switch the gamemode.
    The mod can be https://modrinth.com/mod/switcher
    """)
@ColorBox(id = 1751971538425L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Allow everyone to use `/gamemode` command, except the player Alice.
    Issue the commands:
    1. `/lp group default permission set fuji.permission.gamemode true`
    2. `/lp user Alice permission set fuji.permission.gamemode false`
    """)
@ColorBox(id = 1751971597924L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Allow everyone to use `/gamemode spectator`, but disallows the `/gamemode creative`.
    You have touch the core, and the tricky things.
    Now, issue `/command-permission describe gamemode spectator` command.
    You will find that, the second command node `creative` is actually `an argument`.
    Its `argument` whose argument type is `gamemode`, and accepts the possible values `adventure`, `creative`, `spectator` and `survival`.
    All 4 gamemodes are possible values for `gamemode argument type`.
    That's the reason why you can't just ban the `creative` gamemode.

    If you really want to allow the user to use `/gamemode spectator`, and ban the `/gamemode creative`.
    You can use `command_bundle` to create a `user-level` command, to wrap the `/gamemode` command.
    Like, create a new command named `/switch-to-survival`, as a wrapper command for `/gamemode` command.
    """)
@ColorBox(id = 1751990106002L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Allow players to use `/seed` command.
    The `/seed` command provided by Mojang, requires `level permission` to be `3` to use.
    If you want to `allow` players to use `/seed` command, but you don't want to grant `op` for them.
    Then, you can `set` a `positive string permission` for them.
    Issue `/lp group default permission set fuji.permission.seed true`.
    It says `allow` the players to use `/seed` command.

    Issue `/command-permission describe seed` command, to see how it works.
    """)
@ColorBox(id = 1751990203999L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Dis-allow players to use `/list` command.
    The `/list` command provided by Mojang, requires `level permission` to be `0` to use.
    If you want to `dis-allow` players to use `/list` command.
    But because this command requires no string permission to use.
    So it's impossible to ban it via `luckperms` mod.

    In this case, you `can` set a `negative string permission` for them.
    Issue `/lp group default permission set fuji.permission.list false`.
    It says `dis-allow` the players to use `/list` command.

    Issue `/command-permission describe list` command, to see how it works.
    """)
@ColorBox(id = 1751990343803L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Unset the override of requirement of a specific command.
    To `undo` the operation in the `/seed` example.
    You can `unset` the assigned permission before.
    Issue `/lp group default permission unset fuji.permission.seed` command, to unset the assign permission.
    """)
@ColorBox(id = 1751990466823L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Advanced Usage
    The `luckperms` mod have a feature named `permission context`.
    Which allows you to specify the `per-dimension permission` and `temporary permission`.
    If you are interested, see the details in their official wiki.
    """)



@CommandNode("command-permission")
@CommandRequirement(level = 4)
public class CommandPermissionInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<CommandPermissionConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandPermissionConfigModel.class);

    private static boolean verboseModeFlag = false;

    @DocStringProvider(id = 1752000303860L, value = """
        To use the `command` with that `command path`.
        You need the corresponding permission.

        Issue `/command-permission describe` to see details.
        1. `/command-permission describe fly`
        2. `/command-permission describe fly others @r`
        """)
    public static final PermissionDescriptor COMMAND_PERMISSION_UNIFIED_PERMISSION = new PermissionDescriptor("fuji.permission.<command-path>", 1752000303860L);

    @CommandNode("gui")
    @Document(id = 1751826777672L, value = "Open the command permission gui.")
    public static int $gui(@CommandSource ServerPlayerEntity player) {
        List<CommandNodePermissionWrapper> entities = CommandHelper.Node.getAllCommandNodes().stream()
            .map(CommandNodePermissionWrapper::new)
            .sorted(Comparator.comparing(CommandNodePermissionWrapper::getPath))
            .toList();
        new CommandPermissionGui(player, entities, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826779531L, value = "Toggle the command permission verbose mode.")
    @CommandNode("verbose")
    public static int $verbose(@CommandSource ServerCommandSource source) {
        verboseModeFlag = !verboseModeFlag;

        TextHelper.sendTextByKey(source, verboseModeFlag ? "command_permission.verbose.on" : "command_permission.verbose.off");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826781243L, value = "Describe the `required permissions` of `the given command`.")
    @CommandNode("describe")
    @SuppressWarnings("SameReturnValue")
    public static int $describe(@CommandSource ServerCommandSource source, GreedyString command) {
        /* Parse the command string to get the command context. */
        String $command = command.getValue();
        ParseResults<ServerCommandSource> parseResults = CommandHelper
            .getCommandDispatcher()
            // NOTE: The `parse result` depends on the `command source`.
            .parse($command, source);
        CommandContextBuilder<ServerCommandSource> context = parseResults.getContext();

        /* Describe the command string. */
        String commandString = TextHelper.Parsers.escapeTags(parseResults.getReader().getString());
        TextHelper.sendTextByKey(source,"command_permission.describe.command_string", commandString);

        /* Check if there is early exceptions. */
        @Nullable CommandSyntaxException earlyException = CommandManager.getException(parseResults);
        if (earlyException != null) {
            TextHelper.sendTextByKey(source,"command_permission.describe.command_string.parser.exceptions");
            TextHelper.sendTextByKey(source,"command_permission.describe.command_string.parser.early_exception", earlyException);
            return CommandHelper.Return.SUCCESS;
        }

        /* Report the parser exceptions. */
        var exceptions = parseResults.getExceptions();
        if (!exceptions.isEmpty()) {
            TextHelper.sendTextByKey(source,"command_permission.describe.command_string.parser.exceptions");
            exceptions.forEach((k, v)-> {
                String nodeName = k.getName();
                String exception = v.toString();
                TextHelper.sendTextByKey(source,"command_permission.describe.command_string.parser.exception", nodeName, exception);
            });

            /* Terminate the describing, to avoid misleading. */
            return CommandHelper.Return.SUCCESS;
        }

        /* Describe the command nodes. */
        List<ParsedCommandNode<ServerCommandSource>> nodes = context.getNodes();
        List<String> nodesName = nodes.stream().map(it -> it.getNode().getName()).toList();
        TextHelper.sendTextByKey(source,"command_permission.describe.command_node.nodes", nodesName);

        if (nodesName.isEmpty()) {
            TextHelper.sendTextByKey(source,"command_permission.describe.command_node.empty");
            return CommandHelper.Return.SUCCESS;
        }

        nodes.forEach(it -> {
            var node = it.getNode();
            String nodeName = node.getName();
            String nodeType = CommandHelper.Node.getCommandNodeType(node);
            boolean nodeWrapped = isCommandNodeWrapped(node);
            TextHelper.sendTextByKey(source, "command_permission.describe.command_node.node", nodeName, nodeType, nodeWrapped);
        });

        /* Describe the command path. */
        String commandPath = CommandHelper.Node.joinCommandNodePath(context.getNodes());
        TextHelper.sendTextByKey(source,"command_permission.describe.command_path", commandPath);

        /* Describe the command permissions. */
        TextHelper.sendTextByKey(source,"command_permission.describe.command_permissions");
        List<String> commandPathPrefixes = CommandHelper.Node.getPrefixesOfCommandPath(nodes);
        commandPathPrefixes.forEach(path -> {
            String requiredPermission = COMMAND_PERMISSION_UNIFIED_PERMISSION.withArguments(path);
            TextHelper.sendTextByKey(source,"command_permission.describe.command_permission", requiredPermission);
        });

        /* Newline. */
        TextHelper.sendMessageByText(source, Text.empty());
        return CommandHelper.Return.SUCCESS;
    }

    private static void processVerboseModeFeature(String askWhoForPermissionTestResult, ServerCommandSource source, String commandPath, Tristate commandPermissionTestResult) {
        if (!verboseModeFlag) return;

        // Make description.
        String explanationForPermissionTestResult = makeExplanationForPermissionTestResult(commandPermissionTestResult);

        // Info in console.
        LogUtil.info("""

            ◉ Command Source: {}
            ◉ Command Path of the Target Command: {}
            ◉ Ask who for permission test result: {}
            ◉ Permission Test Result: {}
            ◉ Explanation: {}
            """, source.getName(), commandPath, askWhoForPermissionTestResult, commandPermissionTestResult, explanationForPermissionTestResult);
    }

    private static @NotNull String makeExplanationForPermissionTestResult(Tristate state) {
        String explanation;
        if (state == Tristate.UNDEFINED) {
            explanation = "The permission test result is UNDEFINED, it means command_permission module WILL NOT HANDLE this command. We simply fallback the requirement predicate of this command to its original predicate.";
        } else if (state == Tristate.TRUE) {
            explanation = "The permission test result is TRUE, it means command_permission module WILL ALLOW the command source to use this command.";
        } else if (state == Tristate.FALSE) {
            explanation = "The permission test result is FALSE, it means command_permission module WILL DIS-ALLOW the command source to use this command.";
        } else {
            explanation = "I don't know why, but the value of Tristate is un-expected.";
        }
        return explanation;
    }

    public static @NotNull WrappedPredicate<Object> makeWrappedPredicate(@NotNull com.mojang.brigadier.tree.CommandNode<ServerCommandSource> commandNode, @NotNull Predicate<Object> originalRequirement) {
        return commandSource -> {
            /* If the command source if client command source, use the original predicate. */
            if (commandSource instanceof ServerCommandSource serverCommandSource) {
                /* Ignore the non-player command source. */
                if (serverCommandSource.getPlayer() == null) return originalRequirement.test(serverCommandSource);

                try {
                    /* Compute the command node path. */
                    String commandPath = CommandHelper.Node.findCommandNodePath(commandNode);

                    /* Ask the pre-defined rules if the player can use the command. */
                    String requiredPermissionToExecuteThisCommand = COMMAND_PERMISSION_UNIFIED_PERMISSION.withArguments(commandPath);
                    if (!CommandHelper.Requirement.isAdmin(serverCommandSource)) {
                        for (CommandPermissionRule rule : config.model().rules) {
                            if (requiredPermissionToExecuteThisCommand.matches(rule.permissionPatternRegex)) {
                                Tristate predefinePermissionTestResult = rule.permissionTestResult.toTriState();
                                processVerboseModeFeature("PREDEFINED RULES", serverCommandSource, commandPath, predefinePermissionTestResult);

                                return canUseThisCommand(serverCommandSource, predefinePermissionTestResult, originalRequirement);
                            }
                        }
                    }

                    /* Ask luckperms if the player can use the command. */
                    Tristate luckpermsPermissionTestResult = LuckpermsHelper.getPermission(serverCommandSource.getPlayer().getUuid(), COMMAND_PERMISSION_UNIFIED_PERMISSION, commandPath);
                    processVerboseModeFeature("LUCKPERMS", serverCommandSource, commandPath, luckpermsPermissionTestResult);

                    return canUseThisCommand(serverCommandSource, luckpermsPermissionTestResult, originalRequirement);
                } catch (Throwable useOriginalPredicateIfFailed) {
                    return originalRequirement.test(commandSource);
                }
            } else {
                /* The command source is not ServerCommandSource, simply use the original requirement. */
                return originalRequirement.test(commandSource);
            }
        };
    }

    private static boolean canUseThisCommand(ServerCommandSource source, Tristate permissionTestResult, @NotNull Predicate<Object> originalRequirement) {
        /* If the corresponding permission is DEFINED, we use it to override the original requirement. */
        if (permissionTestResult != Tristate.UNDEFINED) {
            return permissionTestResult.asBoolean();
        }

        /* If the corresponding permission is UNDEFINED, we just fall back to original predicate. */
        return originalRequirement.test(source);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void ensureCommandNodeRequirementIsWrapped() {
        // Enumerate all registered commands, to ensure the getRequirement() is triggered. (For luckperms permission cache)
        CommandHelper.Node
            .getAllCommandNodes()
            .forEach(com.mojang.brigadier.tree.CommandNode::getRequirement);
    }

    public static boolean isCommandNodeWrapped(com.mojang.brigadier.tree.CommandNode<ServerCommandSource> commandNode) {
        return commandNode.getRequirement() instanceof WrappedPredicate<ServerCommandSource>;
    }

    @TestCase(action = "Issue `/reload` command, and check the client command tree.", targets = {
        "The `command_permission` module should warp the newly registered commands."
        , "The client-side command tree should be updated."
    })
    @Override
    protected void onInitialize() {
        CommandEvents.AFTER_REGISTRATION.register((m, d, r, e) -> {
            ensureCommandNodeRequirementIsWrapped();
        });
    }
}
