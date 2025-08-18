package io.github.sakurawald.fuji.core.command.assistant;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.assistant.structure.AvailableNextCommandPath;
import io.github.sakurawald.fuji.core.command.assistant.structure.AvailableNextCommandPathList;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.structure.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class CommandAssistant {

    private static final Map<String, AvailableNextCommandPathList> DEBOUNCE_AVAILABLE_NEXT_COMMAND_PATHS = new HashMap<>();
    private static final Map<String, String> DEBOUNCE_COMPLETED_COMMAND_PATH = new HashMap<>();

    private static @NotNull String toStringByArgumentType(@NotNull CommandNode<ServerCommandSource> commandNode) {
        if (commandNode instanceof ArgumentCommandNode<ServerCommandSource, ?>) {
            return "<" + commandNode.getName() + ">";
        }

        if (commandNode instanceof LiteralCommandNode<ServerCommandSource>) {
            return commandNode.getName();
        }

        return "root";
    }

    private static boolean hasUnparsedCharacters(@NotNull CommandContext<ServerCommandSource> commandContext, @NotNull SuggestionsBuilder builder) {
        ParsedCommandNode<ServerCommandSource> lastCommandNode = getLastParsedCommandNode(commandContext);
        int parsedNodeStart = lastCommandNode.getRange().getStart();
        int suggestionsBuilderStart = builder.getStart();
        return parsedNodeStart != suggestionsBuilderStart;
    }

    private static ParsedCommandNode<ServerCommandSource> getLastParsedCommandNode(@NotNull CommandContext<ServerCommandSource> commandContext) {
        List<ParsedCommandNode<ServerCommandSource>> nodes = commandContext.getNodes();
        return nodes.get(nodes.size() - 1);
    }

    @ForDeveloper("A child command context will be made, if there is any command redirect or command fork.")
    private static @NotNull List<CommandContext<ServerCommandSource>> makeCommandContextChain(@NotNull CommandContext<ServerCommandSource> rootCommandContext) {
        List<CommandContext<ServerCommandSource>> commandContextChain = new ArrayList<>();

        CommandContext<ServerCommandSource> root = rootCommandContext;
        commandContextChain.add(root);

        while (root.getChild() != null) {
            root = root.getChild();
            commandContextChain.add(root);
        }

        return commandContextChain;
    }

    private static @NotNull Pair<CommandContext<ServerCommandSource>, CommandNode<ServerCommandSource>> getLastParsedCommandNodeRecursively(@NotNull CommandContext<ServerCommandSource> rootCommandContext) {
        List<CommandContext<ServerCommandSource>> commandContexts = makeCommandContextChain(rootCommandContext);

        for (int i = commandContexts.size() - 1; i >= 0; i--) {
            CommandContext<ServerCommandSource> currentCommandContext = commandContexts.get(i);
            List<ParsedCommandNode<ServerCommandSource>> nodes = currentCommandContext.getNodes();
            for (int j = nodes.size() - 1; j >= 0; j--) {
                ParsedCommandNode<ServerCommandSource> node = nodes.get(j);
                StringRange range = node.getRange();
                if (range.getStart() != range.getEnd()) {
                    return new Pair<>(currentCommandContext, node.getNode());
                }
            }
        }

        LogUtil.warn("Failed to get last parsed command node from command context chain, falling back to the last command nodes of the root command context {}.", commandContexts);
        return new Pair<>(rootCommandContext, getLastParsedCommandNode(rootCommandContext).getNode());
    }

    @SuppressWarnings("CodeBlock2Expr")
    private static @NotNull String getParsedCommandPath(@NotNull CommandContext<ServerCommandSource> rootCommandContext) {
        StringBuilder sb = new StringBuilder("/");
        List<CommandContext<ServerCommandSource>> commandContexts = makeCommandContextChain(rootCommandContext);

        commandContexts
                .forEach(commandContext -> {
                    commandContext.getNodes().forEach(parsedCommandNode -> {
                        sb.append(toStringByArgumentType(parsedCommandNode.getNode())).append(" ");
                    });
                });
        return sb.toString();
    }

    private static void printUsageForCommandNode(@NotNull ServerCommandSource source, @NotNull CommandContext<ServerCommandSource> rootCommandContext, @NotNull CommandContext<ServerCommandSource> commandContext, @NotNull CommandNode<ServerCommandSource> targetCommandNode, @NotNull SuggestionsBuilder builder) {
        Inspector.inspectCommandNode(targetCommandNode);
        Inspector.inspectSuggestionsBuilder(builder);

        /* Make the output. */
        AvailableNextCommandPathList previousAvailableNextCommandPathList = DEBOUNCE_AVAILABLE_NEXT_COMMAND_PATHS.getOrDefault(source.getName(), new AvailableNextCommandPathList());
        AvailableNextCommandPathList currentAvailableNextCommandPathList = new AvailableNextCommandPathList();

        String inputString = commandContext.getInput();
        for (String value : CommandHelper.getCommandDispatcher().getSmartUsage(targetCommandNode, source).values()) {
            /* Compute prefix string. */
            String prefixString = inputString.substring(0, builder.getStart());

            /* Compute infix string. */
            String infixString = "...";
            if (!hasUnparsedCharacters(commandContext, builder)) {
                infixString = toStringByArgumentType(getLastParsedCommandNode(commandContext).getNode());
            }

            /* Compute suffix string. */
            String suffixString = " " + value;

            /* Trim the strings. */
            prefixString = prefixString.trim();
            infixString = infixString.trim();
            suffixString = suffixString.trim();

            /* Update the strings. */
            currentAvailableNextCommandPathList
                    .getEntries()
                    .add(new AvailableNextCommandPath(prefixString, infixString, suffixString));
        }

        /* Send current available next command path list. */
        if (!currentAvailableNextCommandPathList.equals(previousAvailableNextCommandPathList)) {
            DEBOUNCE_AVAILABLE_NEXT_COMMAND_PATHS.put(source.getName(), currentAvailableNextCommandPathList);
            DEBOUNCE_COMPLETED_COMMAND_PATH.remove(source.getName());

            /* Print the header. */
            Text headerText = TextHelper.getTextByKey(source, "command.assistant.header");
            source.sendMessage(headerText);

            /* Print the body. */
            currentAvailableNextCommandPathList
                    .getEntries()
                    .forEach(entry -> {
                        Text possiblePathText = TextHelper.getTextByKey(source, "command.assistant.incomplete"
                                , TextHelper.Parsers.escapeTags(entry.getPrefixString())
                                , TextHelper.Parsers.escapeTags(entry.getInfixString())
                                , TextHelper.Parsers.escapeTags(entry.getSuffixString()));
                        source.sendMessage(possiblePathText);
                    });

        }

        /* Check if current command node is executable. */
        if (targetCommandNode.getCommand() != null) {
            String previousCompleteCommandPath = DEBOUNCE_COMPLETED_COMMAND_PATH.getOrDefault(source.getName(), "");
            String currentCompletedCommandPath = getParsedCommandPath(rootCommandContext);
            if (!currentCompletedCommandPath.equals(previousCompleteCommandPath)) {
                DEBOUNCE_COMPLETED_COMMAND_PATH.put(source.getName(), currentCompletedCommandPath);
                Text text = TextHelper.getTextByKey(source, "command.assistant.complete", TextHelper.Parsers.escapeTags(currentCompletedCommandPath));
                source.sendMessage(text);
            }
        }
    }

    @TestCase(action = "Test the command assistant.", targets = {
            "Change the `cursor` using mouse click, and see the output."
            , "Test the assistant with command redirect"
            , "Test the assistant at the beginning of the token"
            , "Test the assistant at the end of the token"
            , "Test the assistant with the optional argument: `/back 3`"
            , "Test the assistant with the entity selector: `/send-message `"
    })
    @ForDeveloper("""
            The command suggestions provider will be called:
            1. A new character is inserted or deleted.
            2. The position of the cursor is changed.
            """)
    public static void assist(@NotNull CommandContext<ServerCommandSource> rootCommandContext, @NotNull SuggestionsBuilder builder) {
        Inspector.inspectCommandContext(rootCommandContext, "current");

        Pair<CommandContext<ServerCommandSource>, CommandNode<ServerCommandSource>> pair = getLastParsedCommandNodeRecursively(rootCommandContext);
        CommandContext<ServerCommandSource> targetCommandContext = pair.getKey();
        CommandNode<ServerCommandSource> targetCommandNode = pair.getValue();

        /* Try to use redirect command node. */
        CommandNode<ServerCommandSource> redirect = targetCommandNode.getRedirect();
        if (redirect != null) {
            targetCommandNode = redirect;
        }

        /* Print command usage for target command node. */
        ServerCommandSource source = rootCommandContext.getSource();
        printUsageForCommandNode(source, rootCommandContext, targetCommandContext, targetCommandNode, builder);
    }


    public static class Inspector {

        public static void inspectCommandContext(@NotNull CommandContext<ServerCommandSource> commandContext, @NotNull String walkingPath) {
            LogUtil.info(LogUtil.AnsiColor.BLUE + "◉ Inspect command context {} (path = {})", commandContext, walkingPath);
            LogUtil.info("input string = '{}'", commandContext.getInput());
            LogUtil.info("input string range = {}", commandContext.getRange());
            LogUtil.info("command action = {}", commandContext.getCommand());
            LogUtil.info("root command node = {}", commandContext.getRootNode());
            LogUtil.info("parsed command nodes = {}", commandContext.getNodes());
            LogUtil.info("child command context = {}", commandContext.getChild());
            if (commandContext.getChild() != null) {
                inspectCommandContext(commandContext.getChild(), walkingPath + ".child");
            }
        }

        private static void inspectSuggestionsBuilder(@NotNull SuggestionsBuilder builder) {
            LogUtil.info(LogUtil.AnsiColor.MAGENTA + "◉ Inspect suggestions builder {}", builder);
            LogUtil.info("input string = '{}'", builder.getInput());
            LogUtil.info("remaining string = '{}'", builder.getRemaining());
            LogUtil.info("start = {}", builder.getStart());
        }

        private static void inspectCommandNode(@NotNull CommandNode<ServerCommandSource> commandNode) {
            LogUtil.info(LogUtil.AnsiColor.GREEN + "◉ Inspect command node {}", commandNode);
            LogUtil.info("name = {}", commandNode.getName());
            LogUtil.info("command action = {}", commandNode.getCommand());
            LogUtil.info("redirect command node = {}", commandNode.getRedirect());
            LogUtil.info("is fork = {}", commandNode.isFork());
            LogUtil.info("children command nodes = {}", commandNode.getChildren());
        }
    }

}
