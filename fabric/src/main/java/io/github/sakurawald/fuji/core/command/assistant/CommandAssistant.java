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
import io.github.sakurawald.fuji.core.structure.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class CommandAssistant {

    private static void inspectCommandContext(@NotNull CommandContext<ServerCommandSource> commandContext, @NotNull String walkingPath) {
        LogUtil.info(LogUtil.AnsiColor.BLUE + "◉ Inspect command context {} (path = {})", commandContext, walkingPath);
        LogUtil.info("input string = {}", commandContext.getInput());
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
        LogUtil.info(LogUtil.AnsiColor.YELLOW + "◉ Inspect suggestions builder {}", builder);
        LogUtil.info("input string = {}", builder.getInput());
        LogUtil.info("remaining string = {}", builder.getRemaining());
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

    private static String getFriendlyName(@NotNull CommandNode<ServerCommandSource> commandNode) {
        if (commandNode instanceof ArgumentCommandNode<ServerCommandSource, ?>) {
            return "<" + commandNode.getName() + ">";
        }

        if (commandNode instanceof LiteralCommandNode<ServerCommandSource>) {
            return commandNode.getName();
        }

        return "root";
    }

    private static boolean isInputIncomplete(@NotNull CommandContext<ServerCommandSource> commandContext, @NotNull SuggestionsBuilder builder) {
        ParsedCommandNode<ServerCommandSource> last = commandContext.getNodes().getLast();
        int parsedNodeStart = last.getRange().getStart();
        int suggestionsBuilderStart = builder.getStart();
        return parsedNodeStart != suggestionsBuilderStart;
    }

    private static @NotNull List<CommandContext<ServerCommandSource>> makeCommandContextChain(@NotNull CommandContext<ServerCommandSource> commandContext) {
        List<CommandContext<ServerCommandSource>> commandContextChain = new ArrayList<>();

        CommandContext<ServerCommandSource> root = commandContext;
        commandContextChain.add(root);

        while (root.getChild() != null) {
            root = root.getChild();
            commandContextChain.add(root);
        }

        return commandContextChain;
    }

    private static @NotNull Pair<CommandContext<ServerCommandSource>, CommandNode<ServerCommandSource>> getLastParsedCommandNode(@NotNull CommandContext<ServerCommandSource> rootCommandContext) {
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
        return new Pair<>(rootCommandContext, rootCommandContext.getNodes().getLast().getNode());
    }

    private static void printUsageForCommandNode(@NotNull ServerCommandSource source, @NotNull CommandContext<ServerCommandSource> commandContext, @NotNull CommandNode<ServerCommandSource> targetCommandNode, @NotNull SuggestionsBuilder builder) {
        inspectCommandNode(targetCommandNode);
        inspectSuggestionsBuilder(builder);

        /* Print the header. */
        Text headerText = TextHelper.getTextByKey(source, "command.assistant.header");
        source.sendMessage(headerText);

        /* Print the body. */
        String inputString = commandContext.getInput();

        Map<CommandNode<ServerCommandSource>, String> usage = CommandHelper.getCommandDispatcher().getSmartUsage(targetCommandNode, source);
        for (Map.Entry<CommandNode<ServerCommandSource>, String> entry : usage.entrySet()) {
            CommandNode<ServerCommandSource> key = entry.getKey();
            String value = entry.getValue();

            String prefixString = inputString.substring(0, builder.getStart());
            String infixString = "";

            if (isInputIncomplete(commandContext, builder)) {
                // NOTE: If the remaining string is empty, that means the user just complete an argument, and press the space key.
            } else {
                infixString = getFriendlyName(commandContext.getNodes().getLast().getNode());
            }

            LogUtil.info("key = {}", key);
            String suffixString = " " + value;

            LogUtil.info("prefix string = {}", prefixString);
            LogUtil.info("infix string = {}", infixString);
            LogUtil.info("suffixString = {}", suffixString);

            Text textByValue = TextHelper.getTextByKey(source, "command.assistant.incomplete"
                    , TextHelper.Parsers.escapeTags(prefixString)
                    , TextHelper.Parsers.escapeTags(infixString)
                    , TextHelper.Parsers.escapeTags(suffixString));
            source.sendMessage(textByValue);
        }

        /* Check if current command node is executable. */
        if (targetCommandNode.getCommand() != null) {
            Text text = TextHelper.getTextByKey(source, "command.assistant.complete", TextHelper.Parsers.escapeTags(inputString));
            source.sendMessage(text);
        }
    }



    public static void assist(@NotNull CommandContext<ServerCommandSource> commandContext, @NotNull SuggestionsBuilder builder) {
        inspectCommandContext(commandContext, "current");
        Pair<CommandContext<ServerCommandSource>, CommandNode<ServerCommandSource>> pair = getLastParsedCommandNode(commandContext);
        CommandContext<ServerCommandSource> targetCommandContext = pair.getKey();
        CommandNode<ServerCommandSource> targetCommandNode = pair.getValue();

        /* Try to use redirect command node. */
        CommandNode<ServerCommandSource> redirect = targetCommandNode.getRedirect();
        if (redirect != null) {
            targetCommandNode = redirect;
        }

        /* Print command usage for target command node. */
        ServerCommandSource source = commandContext.getSource();
        printUsageForCommandNode(source, targetCommandContext, targetCommandNode, builder);
    }

}
