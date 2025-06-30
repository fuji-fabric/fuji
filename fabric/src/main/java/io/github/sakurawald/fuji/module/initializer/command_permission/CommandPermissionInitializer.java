package io.github.sakurawald.fuji.module.initializer.command_permission;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
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
@Document("""
    This module provides the `luckperms permissions` for `all commands`.
    """)
@CommandNode("command-permission")
@CommandRequirement(level = 4)
public class CommandPermissionInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<CommandPermissionConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandPermissionConfigModel.class);

    private static boolean verboseModeFlag = false;

    public static final PermissionDescriptor COMMAND_PERMISSION_UNIFIED_PERMISSION = new PermissionDescriptor("fuji.permission.<command-path>", """
        To use the `command` with that `command path`.
        You need the corresponding permission.

        Issue `/command-permission describe` to see details.
        1. `/command-permission describe fly`
        2. `/command-permission describe fly others @r`
        """);

    @CommandNode("gui")
    @Document("Open the command permission gui.")
    public static int $gui(@CommandSource ServerPlayerEntity player) {
        List<CommandNodePermissionWrapper> entities = CommandHelper.getCommandNodes().stream()
            .map(CommandNodePermissionWrapper::new)
            .sorted(Comparator.comparing(CommandNodePermissionWrapper::getPath))
            .toList();
        new CommandPermissionGui(player, entities, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Toggle the command permission verbose mode.")
    @CommandNode("verbose")
    public static int $verbose(@CommandSource ServerCommandSource source) {
        verboseModeFlag = !verboseModeFlag;

        TextHelper.sendMessageByKey(source, verboseModeFlag ? "command_permission.verbose.on" : "command_permission.verbose.off");
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Describe the `required permissions` of `the given command`.")
    @CommandNode("describe")
    public static int $describe(@CommandSource ServerCommandSource source, GreedyString command) {
        /* Parse the command string to get the command context. */
        String $command = command.getValue();
        ParseResults<ServerCommandSource> parseResults = ServerHelper
            .getCommandDispatcher()
            // NOTE: The `parse result` depends on the `command source`.
            .parse($command, source);
        CommandContextBuilder<ServerCommandSource> context = parseResults.getContext();

        /* Describe the command string. */
        String commandString = TextHelper.escapeTags(parseResults.getReader().getString());
        TextHelper.sendMessageByKey(source,"command_permission.describe.command_string", commandString);

        /* Check if there is early exceptions. */
        @Nullable CommandSyntaxException earlyException = CommandManager.getException(parseResults);
        if (earlyException != null) {
            TextHelper.sendMessageByKey(source,"command_permission.describe.command_string.parser.exceptions");
            TextHelper.sendMessageByKey(source,"command_permission.describe.command_string.parser.early_exception", earlyException);
            return CommandHelper.Return.SUCCESS;
        }

        /* Report the parser exceptions. */
        var exceptions = parseResults.getExceptions();
        if (!exceptions.isEmpty()) {
            TextHelper.sendMessageByKey(source,"command_permission.describe.command_string.parser.exceptions");
            exceptions.forEach((k, v)-> {
                String nodeName = k.getName();
                String exception = v.toString();
                TextHelper.sendMessageByKey(source,"command_permission.describe.command_string.parser.exception", nodeName, exception);
            });

            /* Terminate the describing, to avoid misleading. */
            return CommandHelper.Return.SUCCESS;
        }

        /* Describe the command nodes. */
        List<ParsedCommandNode<ServerCommandSource>> nodes = context.getNodes();
        List<String> nodesName = nodes.stream().map(it -> it.getNode().getName()).toList();
        TextHelper.sendMessageByKey(source,"command_permission.describe.command_node.nodes", nodesName);

        if (nodesName.isEmpty()) {
            TextHelper.sendMessageByKey(source,"command_permission.describe.command_node.empty");
            return CommandHelper.Return.SUCCESS;
        }

        nodes.forEach(it -> {
            var node = it.getNode();
            String nodeName = node.getName();
            String nodeType = CommandHelper.getCommandNodeType(node);
            boolean nodeWrapped = isCommandNodeWrapped(node);
            TextHelper.sendMessageByKey(source, "command_permission.describe.command_node.node", nodeName, nodeType, nodeWrapped);
        });

        /* Describe the command path. */
        String commandPath = CommandHelper.joinCommandNodePath(context.getNodes());
        TextHelper.sendMessageByKey(source,"command_permission.describe.command_path", commandPath);

        /* Describe the command permissions. */
        TextHelper.sendMessageByKey(source,"command_permission.describe.command_permissions");
        List<String> commandPathPrefixes = CommandHelper.getPrefixesOfCommandPath(nodes);
        commandPathPrefixes.forEach(path -> {
            String requiredPermission = COMMAND_PERMISSION_UNIFIED_PERMISSION.withArguments(path);
            TextHelper.sendMessageByKey(source,"command_permission.describe.command_permission", requiredPermission);
        });

        /* Newline. */
        source.sendMessage(Text.empty());
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

    public static @NotNull WrappedPredicate<ServerCommandSource> makeWrappedPredicate(String commandPath, @NotNull Predicate<ServerCommandSource> originalRequirement) {
        return source -> {
            /* Ignore the non-player command source. */
            if (source.getPlayer() == null) return originalRequirement.test(source);

            try {
                /* Ask the pre-defined rules if the player can use the command. */
                String requiredPermissionToExecuteThisCommand = COMMAND_PERMISSION_UNIFIED_PERMISSION.withArguments(commandPath);
                if (!PlayerHelper.isAdmin(source)) {
                    for (CommandPermissionRule rule : config.model().rules) {
                        if (requiredPermissionToExecuteThisCommand.matches(rule.permissionPatternRegex)) {
                            Tristate predefinePermissionTestResult = rule.permissionTestResult.toTriState();
                            processVerboseModeFeature("PREDEFINED RULES", source, commandPath, predefinePermissionTestResult);

                            return canUseThisCommand(source, predefinePermissionTestResult, originalRequirement);
                        }
                    }
                }

                /* Ask luckperms if the player can use the command. */
                Tristate luckpermsPermissionTestResult = LuckpermsHelper.getPermission(source.getPlayer().getUuid(), COMMAND_PERMISSION_UNIFIED_PERMISSION, commandPath);
                processVerboseModeFeature("LUCKPERMS", source, commandPath, luckpermsPermissionTestResult);

                return canUseThisCommand(source, luckpermsPermissionTestResult, originalRequirement);
            } catch (Throwable useOriginalPredicateIfFailed) {
                return originalRequirement.test(source);
            }
        };
    }

    private static boolean canUseThisCommand(ServerCommandSource source, Tristate permissionTestResult, @NotNull Predicate<ServerCommandSource> originalRequirement) {
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
        CommandHelper
            .getCommandNodes()
            .forEach(com.mojang.brigadier.tree.CommandNode::getRequirement);
    }

    public static boolean isCommandNodeWrapped(com.mojang.brigadier.tree.CommandNode<ServerCommandSource> commandNode) {
        return commandNode.getRequirement() instanceof WrappedPredicate<ServerCommandSource>;
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> ensureCommandNodeRequirementIsWrapped());
    }

}
