package mod.fuji.core.command.assistant;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.assistant.structure.AvailableNextCommandPath;
import mod.fuji.core.command.assistant.structure.AvailableNextCommandPathList;
import mod.fuji.core.config.Configs;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.structure.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class CommandAssistant {

    private static final Map<String, AvailableNextCommandPathList> DEBOUNCE_AVAILABLE_NEXT_COMMAND_PATHS = new ConcurrentHashMap<>();
    private static final Map<String, String> DEBOUNCE_COMPLETED_COMMAND_PATH = new ConcurrentHashMap<>();

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
        return suggestionsBuilderStart != parsedNodeStart;
    }

    private static ParsedCommandNode<ServerCommandSource> getLastParsedCommandNode(@NotNull CommandContext<ServerCommandSource> commandContext) {
        List<ParsedCommandNode<ServerCommandSource>> parsedCommandNodes = commandContext.getNodes();
        return parsedCommandNodes.get(parsedCommandNodes.size() - 1);
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

    private static @NotNull Pair<CommandContext<ServerCommandSource>, CommandNode<ServerCommandSource>> getAssistanceTargetCommandNode(@NotNull CommandContext<ServerCommandSource> rootCommandContext) {
        List<CommandContext<ServerCommandSource>> commandContextChain = makeCommandContextChain(rootCommandContext);

        for (int i = commandContextChain.size() - 1; i >= 0; i--) {
            CommandContext<ServerCommandSource> currentCommandContext = commandContextChain.get(i);
            List<ParsedCommandNode<ServerCommandSource>> currentParsedCommandNodes = currentCommandContext.getNodes();
            for (int j = currentParsedCommandNodes.size() - 1; j >= 0; j--) {
                ParsedCommandNode<ServerCommandSource> candidateParsedCommandNode = currentParsedCommandNodes.get(j);

                // NOTE: For GreedyString argument type, if its value is empty, then you will get `<argument rest:string()>@StringRange{start=39, end=39}`
                StringRange range = candidateParsedCommandNode.getRange();
                if (range.getStart() != range.getEnd()) {
                    return new Pair<>(currentCommandContext, candidateParsedCommandNode.getNode());
                }
            }
        }

        LogUtil.warn("Failed to get last parsed command node from command context chain, falling back to the last command nodes of the root command context {}.", commandContextChain);
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

    private static void printUsageForCommandNode(@NotNull ServerCommandSource commandSource, @NotNull CommandContext<ServerCommandSource> rootCommandContext, @NotNull CommandContext<ServerCommandSource> commandContext, @NotNull CommandNode<ServerCommandSource> targetCommandNode, @NotNull SuggestionsBuilder builder) {
//        Inspector.inspectCommandNode(targetCommandNode);
//        Inspector.inspectSuggestionsBuilder(builder);

        /* Compute the possible next command path list. */
        // NOTE: Handle the edge-case for `/tpadeny all` and `/tpadeny <target>`. (When literal `al` becomes literal `all`)
        AtomicBoolean headerMessagePrinted = new AtomicBoolean(false);
        AvailableNextCommandPathList currentAvailableNextCommandPathList = new AvailableNextCommandPathList();

        String inputString = commandContext.getInput();
        for (String value : CommandHelper.getCommandDispatcher().getSmartUsage(targetCommandNode, commandSource).values()) {
            /* Compute prefix string: truncate the input string to make room for infix string. */
            String prefixString = inputString.substring(0, builder.getStart());

            /* Compute infix string: the infix string can be pending state representation `...` or a selected command path. */
            String infixString = "..."; // There are multiple possible command paths, pending for more information.
            if (!hasUnparsedCharacters(commandContext, builder)) {
                // The user has selected a command path, now we have the information to print the selected argument name.
                infixString = toStringByArgumentType(getLastParsedCommandNode(commandContext).getNode());
            }

            /* Compute suffix string. */
            String suffixString = " " + value;

            /* Trim the strings, to improve cache hits. */
            prefixString = prefixString.trim();
            infixString = infixString.trim();
            suffixString = suffixString.trim();

            /* Update the strings. */
            currentAvailableNextCommandPathList
                    .getEntries()
                    .add(new AvailableNextCommandPath(prefixString, infixString, suffixString));
        }

        /* Debounce and send current available next command path list. */
        DEBOUNCE_AVAILABLE_NEXT_COMMAND_PATHS.compute(commandSource.getName(), (key, previousValue) -> {
            /* Debounce and send. */
            if (!currentAvailableNextCommandPathList.equals(previousValue)) {
                // NOTE: Re-send the completed command path, as the old message has already been buried.
                DEBOUNCE_COMPLETED_COMMAND_PATH.remove(commandSource.getName());

                /* Print the header. */
                if (!currentAvailableNextCommandPathList.getEntries().isEmpty()) {
                    // NOTE: Only print the header if the entries are not empty. (For `/back abc` command)
                    printCommandAssistantHeaderIfAbsent(headerMessagePrinted, commandSource);
                }

                /* Print the body. */
                currentAvailableNextCommandPathList
                    .getEntries()
                    .forEach(entry -> {
                        Text possiblePathText = TextHelper.getTextByKey(commandSource, "command.assistant.incomplete"
                            , TextHelper.Parsers.escapeTags(entry.getPrefixString())
                            , TextHelper.Parsers.escapeTags(entry.getInfixString())
                            , TextHelper.Parsers.escapeTags(entry.getSuffixString()));
                        commandSource.sendMessage(possiblePathText);
                    });

            }

            /* Update the value. */
            return currentAvailableNextCommandPathList;
        });

        /* Debounce and send the completed command path. */
        if (CommandHelper.Node.isExecutableCommandNode(targetCommandNode)) {
            String currentCompletedCommandPath = getParsedCommandPath(rootCommandContext);
            DEBOUNCE_COMPLETED_COMMAND_PATH.compute(commandSource.getName(), (key, previousValue) -> {
                /* Debounce and send. */
                if (!currentCompletedCommandPath.equals(previousValue)) {
                    printCommandAssistantHeaderIfAbsent(headerMessagePrinted, commandSource);

                    Text text = TextHelper.getTextByKey(commandSource, "command.assistant.complete", TextHelper.Parsers.escapeTags(currentCompletedCommandPath));
                    commandSource.sendMessage(text);
                }

                /* Update the value. */
                return currentCompletedCommandPath;
            });

        }
    }

    private static void printCommandAssistantHeaderIfAbsent(@NotNull AtomicBoolean headerPrinted, @NotNull ServerCommandSource commandSource) {
        if (headerPrinted.compareAndSet(false, true)) {
            Text headerText = TextHelper.getTextByKey(commandSource, "command.assistant.header");
            commandSource.sendMessage(headerText);
        }
    }

    @TestCase(action = "Test the command assistant.", targets = {
            "Change the `cursor` using mouse click, and see the output."
            , "Test the assistant with command redirect"
            , "Test the assistant at the beginning of the token"
            , "Test the assistant at the end of the token"
            , "Test the assistant with the optional argument: `/back 3`"
            , "Test the assistant with the entity selector: `/send-message @r`"
            , "Test the assistant with custom parser and non-zero-offset suggestions builder: `/fly others @a[distance=..8`"
    })
    @ForDeveloper("""
            1. The custom command suggestions provider will be called when the cursor enters, leaves, or moves within a required argument. (Except case 2.)
            2. If the client use `Tab` key or `Shift + Tab` key to change the `input` and `cursor`, then the custom command suggestion provider will not be called.
            """)
    public static void assist(@NotNull CommandContext<ServerCommandSource> rootCommandContext, @NotNull SuggestionsBuilder builder) {
        if (canUseCommandAssistant(rootCommandContext)) {
            return;
        }

//        Inspector.inspectCommandContext(rootCommandContext, "current");

        Pair<CommandContext<ServerCommandSource>, CommandNode<ServerCommandSource>> pair = getAssistanceTargetCommandNode(rootCommandContext);
        CommandContext<ServerCommandSource> targetCommandContext = pair.getKey();
        CommandNode<ServerCommandSource> targetCommandNode = pair.getValue();

        /* Perform the forward action for target command node, if there is any redirect target. */
        // NOTE: If you didn't perform the forward action, then there is no result for next available paths. (e.g. `/help send-message @s --silent true`)
        targetCommandNode = performForwardAction(targetCommandNode);

        /* Print command usage for target command node. */
        ServerCommandSource source = rootCommandContext.getSource();
        printUsageForCommandNode(source, rootCommandContext, targetCommandContext, targetCommandNode, builder);
    }

    private static @NotNull CommandNode<ServerCommandSource> performForwardAction(CommandNode<ServerCommandSource> targetCommandNode) {
        CommandNode<ServerCommandSource> redirectTargetCommandNode = targetCommandNode.getRedirect();
        if (redirectTargetCommandNode != null) {
            targetCommandNode = redirectTargetCommandNode;
        }
        return targetCommandNode;
    }

    private static boolean canUseCommandAssistant(@NotNull CommandContext<ServerCommandSource> rootCommandContext) {
        return !rootCommandContext.getSource().hasPermissionLevel(Configs.MAIN_CONTROL_CONFIG.model().core.command.assistant.requirement.level_permission);
    }

    @SuppressWarnings("unused")
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
