package mod.fuji.core.command.assistant;

import com.google.common.collect.EvictingQueue;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.assistant.structure.AvailableNextCommandPath;
import mod.fuji.core.command.assistant.structure.AvailableNextCommandPathList;
import mod.fuji.core.config.Configs;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerLeftEvent;
import mod.fuji.core.structure.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CommandAssistant {

    private static final Map<String, EvictingQueue<AvailableNextCommandPathList>> DEBOUNCE_AVAILABLE_NEXT_COMMAND_PATHS = new ConcurrentHashMap<>();
    private static final Map<String, EvictingQueue<String>> DEBOUNCE_COMPLETED_COMMAND_PATH = new ConcurrentHashMap<>();
    private static final int DEBOUNCE_QUEUE_SIZE = 5;

    @EventConsumer
    private static void resetCommandAssistantCache(@Unused PlayerLeftEvent event) {
        String playerName = PlayerHelper.getPlayerName(event.getPlayer());
        DEBOUNCE_AVAILABLE_NEXT_COMMAND_PATHS.remove(playerName);
        DEBOUNCE_COMPLETED_COMMAND_PATH.remove(playerName);
    }

    private static @NotNull String toStringByArgumentType(@NotNull CommandNode<CommandSourceStack> commandNode) {
        if (commandNode instanceof ArgumentCommandNode<CommandSourceStack, ?>) {
            return "<" + commandNode.getName() + ">";
        }

        if (commandNode instanceof LiteralCommandNode<CommandSourceStack>) {
            return commandNode.getName();
        }

        return "root";
    }

    private static boolean hasUnparsedCharacters(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull SuggestionsBuilder builder) {
        ParsedCommandNode<CommandSourceStack> lastCommandNode = getLastParsedCommandNode(commandContext);
        int parsedNodeStart = lastCommandNode.getRange().getStart();
        int suggestionsBuilderStart = builder.getStart();
        return suggestionsBuilderStart != parsedNodeStart;
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    private static ParsedCommandNode<CommandSourceStack> getLastParsedCommandNode(@NotNull CommandContext<CommandSourceStack> commandContext) {
        List<ParsedCommandNode<CommandSourceStack>> parsedCommandNodes = commandContext.getNodes();
        return parsedCommandNodes.get(parsedCommandNodes.size() - 1);
    }

    /**
     * A child command context will be made, if there is any command redirect or command fork.
     **/
    private static @NotNull List<CommandContext<CommandSourceStack>> makeCommandContextChain(@NotNull CommandContext<CommandSourceStack> rootCommandContext) {
        List<CommandContext<CommandSourceStack>> commandContextChain = new ArrayList<>();

        CommandContext<CommandSourceStack> root = rootCommandContext;
        commandContextChain.add(root);

        while (root.getChild() != null) {
            root = root.getChild();
            commandContextChain.add(root);
        }

        return commandContextChain;
    }

    private static @NotNull Pair<CommandContext<CommandSourceStack>, CommandNode<CommandSourceStack>> getAssistanceTargetCommandNode(@NotNull CommandContext<CommandSourceStack> rootCommandContext) {
        List<CommandContext<CommandSourceStack>> commandContextChain = makeCommandContextChain(rootCommandContext);

        for (int i = commandContextChain.size() - 1; i >= 0; i--) {
            CommandContext<CommandSourceStack> currentCommandContext = commandContextChain.get(i);
            List<ParsedCommandNode<CommandSourceStack>> currentParsedCommandNodes = currentCommandContext.getNodes();
            for (int j = currentParsedCommandNodes.size() - 1; j >= 0; j--) {
                ParsedCommandNode<CommandSourceStack> candidateParsedCommandNode = currentParsedCommandNodes.get(j);

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
    private static @NotNull String getParsedCommandPath(@NotNull CommandContext<CommandSourceStack> rootCommandContext) {
        StringBuilder sb = new StringBuilder("/");
        List<CommandContext<CommandSourceStack>> commandContexts = makeCommandContextChain(rootCommandContext);

        commandContexts
            .forEach(commandContext -> {
                commandContext.getNodes().forEach(parsedCommandNode -> {
                    sb.append(toStringByArgumentType(parsedCommandNode.getNode())).append(" ");
                });
            });
        return sb.toString();
    }

    public static @NotNull String ensurePrefix(@NotNull String input, @NotNull String prefix) {
        return input.startsWith(prefix) ? input : prefix + input;
    }

    private static void printUsageForCommandNode(@NotNull CommandSourceStack commandSource, @NotNull CommandContext<CommandSourceStack> rootCommandContext, @NotNull CommandContext<CommandSourceStack> commandContext, @NotNull CommandNode<CommandSourceStack> targetCommandNode, @NotNull SuggestionsBuilder builder) {
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

            /* Compute infix string: the infix string can be pending state structure `...` or a selected command path. */
            String infixString = "..."; // There are multiple possible command paths, pending for more information.
            if (!hasUnparsedCharacters(commandContext, builder)) {
                // The user has selected a command path, now we have the information to print the selected argument name.
                infixString = toStringByArgumentType(getLastParsedCommandNode(commandContext).getNode());
            }

            /* Compute suffix string. */
            String suffixString = " " + value;

            /* Trim the strings, to improve cache hits. */
            prefixString = ensurePrefix(prefixString.trim(), "/");
            infixString = infixString.trim();
            suffixString = suffixString.trim();

            /* Update the strings. */
            currentAvailableNextCommandPathList
                .getEntries()
                .add(new AvailableNextCommandPath(prefixString, infixString, suffixString));
        }

        /* Debounce and send current available next command path list. */
        DEBOUNCE_AVAILABLE_NEXT_COMMAND_PATHS.compute(commandSource.getTextName(), (key, previousValue) -> {
            if (previousValue == null) {
                previousValue = EvictingQueue.create(DEBOUNCE_QUEUE_SIZE);
            }

            /* Debounce and send. */
            if (!previousValue.contains(currentAvailableNextCommandPathList)) {
                // NOTE: Re-send the completed command path, as the old message has already been buried.
                DEBOUNCE_COMPLETED_COMMAND_PATH.remove(commandSource.getTextName());

                /* Print the header. */
                if (!currentAvailableNextCommandPathList.getEntries().isEmpty()) {
                    // NOTE: Only print the header if the entries are not empty. (For `/back abc` command)
                    printCommandAssistantHeaderIfAbsent(headerMessagePrinted, commandSource);
                }

                /* Print the body. */
                currentAvailableNextCommandPathList
                    .getEntries()
                    .forEach(entry -> {
                        Component possiblePathText = TextHelper.getTextByKey(commandSource, "command.assistant.incomplete"
                            , TextHelper.Parsers.escapeTags(entry.getPrefixString())
                            , TextHelper.Parsers.escapeTags(entry.getInfixString())
                            , TextHelper.Parsers.escapeTags(entry.getSuffixString()));
                        commandSource.sendSystemMessage(possiblePathText);
                    });

                /* Add the value. */
                previousValue.add(currentAvailableNextCommandPathList);
            }

            return previousValue;
        });

        /* Debounce and send the completed command path. */
        if (CommandHelper.Node.isExecutableCommandNode(targetCommandNode)) {
            String currentCompletedCommandPath = getParsedCommandPath(rootCommandContext);
            DEBOUNCE_COMPLETED_COMMAND_PATH.compute(commandSource.getTextName(), (key, previousValue) -> {
                if (previousValue == null) {
                    previousValue = EvictingQueue.create(DEBOUNCE_QUEUE_SIZE);
                }

                /* Debounce and send. */
                if (!previousValue.contains(currentCompletedCommandPath)) {
                    printCommandAssistantHeaderIfAbsent(headerMessagePrinted, commandSource);

                    Component text = TextHelper.getTextByKey(commandSource, "command.assistant.complete", TextHelper.Parsers.escapeTags(currentCompletedCommandPath));
                    commandSource.sendSystemMessage(text);

                    /* Add the value. */
                    previousValue.add(currentCompletedCommandPath);
                }

                return previousValue;
            });

        }
    }

    private static void printCommandAssistantHeaderIfAbsent(@NotNull AtomicBoolean headerPrinted, @NotNull CommandSourceStack commandSource) {
        if (headerPrinted.compareAndSet(false, true)) {
            Component headerText = TextHelper.getTextByKey(commandSource, "command.assistant.header");
            commandSource.sendSystemMessage(headerText);
        }
    }

    /**
     * 1. The custom command suggestions provider will be called when the cursor enters, leaves, or moves within a required argument. (Except case 2.)
     * <p>
     * 2. If the client use `Tab` key or `Shift + Tab` key to change the `input` and `cursor`, then the custom command suggestion provider will not be called.
     **/
    @TestCase(action = "Test the command assistant.", targets = {
        "Change the `cursor` using mouse click, and see the output."
        , "Test the assistant with command redirect"
        , "Test the assistant at the beginning of the token"
        , "Test the assistant at the end of the token"
        , "Test the assistant with the optional argument: `/back 3`"
        , "Test the assistant with the entity selector: `/send-message @r`"
        , "Test the assistant with custom parser and non-zero-offset suggestions builder: `/fly others @a[distance=..8`"
    })
    @TestCase(action = "Test the de-bounce for greedy command string arguments.", targets = {
        "Issue: `/IF send-message %player:name% 1 THEN send-broadcast 22...`",
        "Issue: `/IF   send-message %player:name% 1 THEN send-broadcast 2  ELSE  send-chat %player:name% 3`"
    })
    public static void assist(@NotNull CommandContext<CommandSourceStack> rootCommandContext, @NotNull SuggestionsBuilder builder) {
        if (!canUseCommandAssistant(rootCommandContext)) {
            return;
        }

//        Inspector.inspectCommandContext(rootCommandContext, "current");

        Pair<CommandContext<CommandSourceStack>, CommandNode<CommandSourceStack>> pair = getAssistanceTargetCommandNode(rootCommandContext);
        CommandContext<CommandSourceStack> targetCommandContext = pair.getKey();
        CommandNode<CommandSourceStack> targetCommandNode = pair.getValue();

        /* Perform the forward action for target command node, if there is any redirect target. */
        // NOTE: If you didn't perform the forward action, then there is no result for next available paths. (e.g. `/help send-message @s --silent true`)
        targetCommandNode = performForwardAction(targetCommandNode);

        /* Print command usage for target command node. */
        CommandSourceStack source = rootCommandContext.getSource();
        printUsageForCommandNode(source, rootCommandContext, targetCommandContext, targetCommandNode, builder);
    }

    private static @NotNull CommandNode<CommandSourceStack> performForwardAction(CommandNode<CommandSourceStack> targetCommandNode) {
        CommandNode<CommandSourceStack> redirectTargetCommandNode = targetCommandNode.getRedirect();
        if (redirectTargetCommandNode != null) {
            targetCommandNode = redirectTargetCommandNode;
        }
        return targetCommandNode;
    }

    private static boolean canUseCommandAssistant(@NotNull CommandContext<CommandSourceStack> rootCommandContext) {
        CommandSourceStack source = rootCommandContext.getSource();
        int levelPermission = Configs.MAIN_CONTROL_CONFIG.model().core.command.assistant.requirement.level_permission;
        return CommandHelper.Requirement.hasLevelPermission(source, levelPermission);
    }

    @SuppressWarnings("unused")
    public static class Inspector {

        public static void inspectCommandContext(@NotNull CommandContext<CommandSourceStack> commandContext, @NotNull String walkingPath) {
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

        private static void inspectCommandNode(@NotNull CommandNode<CommandSourceStack> commandNode) {
            LogUtil.info(LogUtil.AnsiColor.GREEN + "◉ Inspect command node {}", commandNode);
            LogUtil.info("name = {}", commandNode.getName());
            LogUtil.info("command action = {}", commandNode.getCommand());
            LogUtil.info("redirect command node = {}", commandNode.getRedirect());
            LogUtil.info("is fork = {}", commandNode.isFork());
            LogUtil.info("children command nodes = {}", commandNode.getChildren());
        }
    }

}
