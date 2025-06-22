package io.github.sakurawald.module.initializer.command_permission;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.core.annotation.Cite;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_permission.gui.CommandPermissionGui;
import io.github.sakurawald.module.initializer.command_permission.structure.CommandNodePermission;
import io.github.sakurawald.module.initializer.command_permission.structure.WrappedPredicate;
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

    private static boolean verboseModeFlag = false;

    @CommandNode("gui")
    @Document("Open the command permission gui.")
    public static int $gui(@CommandSource ServerPlayerEntity player) {
        List<CommandNodePermission> entities = CommandHelper.getCommandNodes().stream()
            .map(CommandNodePermission::new)
            .sorted(Comparator.comparing(CommandNodePermission::getPath))
            .toList();
        new CommandPermissionGui(player, entities, 0).open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("verbose")
    @Document("Toggle the command permission verbose mode.")
    public static int $verbose(@CommandSource ServerCommandSource source) {
        verboseModeFlag = !verboseModeFlag;

        TextHelper.sendMessageByKey(source, verboseModeFlag ? "command_permission.verbose.on" : "command_permission.verbose.off");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("describe")
    @Document("Describe the command path and required permissions of a give command.")
    public static int $describe(@CommandSource ServerCommandSource source, GreedyString command) {
        /* Parse the command string to get the command context. */
        String $command = command.getValue();
        ParseResults<ServerCommandSource> parseResults = ServerHelper
            .getCommandDispatcher()
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
        String commandPath = CommandHelper.computeCommandNodePath(context.getNodes());
        TextHelper.sendMessageByKey(source,"command_permission.describe.command_path", commandPath);

        /* Describe the command permissions. */
        TextHelper.sendMessageByKey(source,"command_permission.describe.command_permissions");
        List<String> commandPathPrefixes = CommandHelper.getCommandPathPrefixes(nodes);
        commandPathPrefixes.forEach(path -> {
            String requiredPermission = computeCommandPermission(path);
            TextHelper.sendMessageByKey(source,"command_permission.describe.command_permission", requiredPermission);
        });

        /* Newline. */
        source.sendMessage(Text.empty());
        return CommandHelper.Return.SUCCESS;
    }


    public static String computeCommandPermission(String commandPath) {
        return "fuji.permission.%s".formatted(commandPath);
    }

    private static void processVerboseMode(ServerCommandSource source, String commandPath, String requiredPermissionToExecuteThisCommand, Tristate state) {
        if (!verboseModeFlag) return;

        // Make description.
        String description = makeDescription(state);

        // Info in console.
        LogUtil.info("""

            ◉ Command Source: {}
            ◉ Command Path of the Target Command: {}
            ◉ The permission used by Luckperms to calculate whether the command source can use the target command: {}
            ◉ LuckPerms Permission Calculation Result: {}
            ◉ Description: {}
            """, source.getName(), commandPath, requiredPermissionToExecuteThisCommand, state, description);
    }

    private static @NotNull String makeDescription(Tristate state) {
        String description;
        if (state == Tristate.UNDEFINED) {
            description = "The luckperms permission test result is UNDEFINED, it means command_permission module WILL NOT HANDLE this command. We simply fallback the requirement predicate of this command to its original predicate.";
        } else if (state == Tristate.TRUE) {
            description = "The luckperms permission test result is TRUE, it means command_permission module WILL ALLOW the command source to use this command.";
        } else if (state == Tristate.FALSE) {
            description = "The luckperms permission test result is FALSE, it means command_permission module WILL DIS-ALLOW the command source to use this command.";
        } else {
            description = "I don't know why, but the value of Tristate is un-expected.";
        }
        return description;
    }

    public static @NotNull WrappedPredicate<ServerCommandSource> makeWrappedPredicate(String commandPath, @NotNull Predicate<ServerCommandSource> original) {
        return source -> {
            /* Ignore the non-player command source. */
            if (source.getPlayer() == null) return original.test(source);

            /* Try to use the wrapped predicate. */
            try {
                /* By default, command /seed has no permission. So we can create a wrapped-permission "fuji.seed"
                   and then grant this permission to anyone so that he can use /seed command.
                   And also set other's permission fuji.seed false to dis-allow them to use /seed command.
                   If a command doesn't have a wrapped-permission, then it will use the original requirement-supplier.

                   Only valid command has its command path (command-alias also has its path, but it will redirect the execution to the real command-path)
                 */
                String permission = computeCommandPermission(commandPath);
                Tristate triState = PermissionHelper.getPermission(source.getPlayer().getUuid(), permission);
                processVerboseMode(source, commandPath, permission, triState);

                /* If the related permission is UNDEFINED, we just fall back to original predicate. */
                if (triState != Tristate.UNDEFINED) {
                    return triState.asBoolean();
                }
                return original.test(source);
            } catch (Throwable use_original_predicate_if_failed) {
                return original.test(source);
            }
        };
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
