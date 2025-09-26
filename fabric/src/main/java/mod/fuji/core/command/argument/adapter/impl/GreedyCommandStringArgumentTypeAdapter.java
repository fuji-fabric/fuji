package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.GreedyCommandString;
import mod.fuji.core.document.annotation.TestCase;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class GreedyCommandStringArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(GreedyCommandString.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("greedy-command-string");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.greedyString();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return new GreedyCommandString(string);
    }

    final Set<String> greedyStringSeparatorLiterals;

    public GreedyCommandStringArgumentTypeAdapter() {
        this.greedyStringSeparatorLiterals = Set.of();
    }

    public GreedyCommandStringArgumentTypeAdapter(@NotNull Set<String> greedyStringSeparatorLiterals) {
        this.greedyStringSeparatorLiterals = greedyStringSeparatorLiterals;
    }

    @TestCase(action = "Test the functionality for recursive suggestions builder. (No separator literals)", targets = {
        "Issue: `/run as console send-broadcast <rb>I am %player:name%`",
        "Issue: `/run as player @s run as console run as fake-op %player:name% say I am %player:name%`",
        "Issue: `/run as console command-attachment attach-entity-one @e[type=...`",
        "Issue: `/NOT NOT NOT run as console delay 3 foreach when-online %player:name% send-broadcast You are %player:name%`"
    })
    @TestCase(action = "Test the functionality of placeholders.", targets = {
        "Issue: `/chain run as fake-op @s run as console say 1 chain say 2`",
        "Issue: `/chain run as fake-op %player:name% sa`",
        "Issue: `/chain run as fake-op %player:name% run as console run as player @r say 1 chain say 2`",
        "Issue: `/run as player SakuraWald run as console run as fake-op %player:name% send-message @s I am %player:name%`"
    })
    @TestCase(action = "Test the functionality for recursive suggestions builder. (With separator literals)", targets = {
        "Issue: `/chain say 1 chain`",
        "Issue: `/chain say 1 chain say 2 chain`",
        "Issue: `/chain say 1 chain        say     2     chain say 3`",
        "Issue: `/chain say 1 chain`",
        "Issue: `/chain say 1 chain chain chain sa`",
        "Issue: `/chain say 3  chain   send-messa`",
        "Issue: `/chain say 3  chain   send-message   @s     <rb>Hello`"
    })
    @Override
    protected @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests((context, builder) -> {
                /* Skip for non-player command source. */
                if (context.getSource().getPlayer() == null) {
                    return builder.buildFuture();
                }
                ServerPlayerEntity player = context.getSource().getPlayer();

                /* Define the input string. */
                @NotNull final String input = builder.getInput();
                @NotNull final String inputTrim = input.trim();
                LogUtil.debug("input = '{}'", input);
                LogUtil.debug("builder.getStart() = {}", builder.getStart());

                /* Suggest command suggestions from remaining string. */
                builder.add(makeCommandStringSuggestionsBuilder(player, builder));

                /* Suggest separator literals at any point. */
                makeSeparatorLiteralSuggestionsBuilders(inputTrim, builder)
                    .forEach(builder::add);

                /* Merge the results into the final builder. */
                return builder.buildFuture();
            });
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private @NotNull SuggestionsBuilder getBoundedBuilder(@NotNull SuggestionsBuilder builder) {
        /* Bound the builder with declared separator literals. */
        for (String literal : greedyStringSeparatorLiterals) {
            final String remainingString = builder.getRemaining();

            final String token = " " + literal + " ";
            final int index = remainingString.lastIndexOf(token);
            if (index != -1) {
                final int offset = index + token.length();
                SuggestionsBuilder boundedBuilder = builder.createOffset(builder.getStart() + offset);
                return boundedBuilder;
            }
        }

        /* No separator literals found, simply return the original builder. */
        return builder;
    }

    private SuggestionsBuilder makeCommandStringSuggestionsBuilder(@NotNull ServerPlayerEntity player, @NotNull SuggestionsBuilder builder) {
        /* Make bounded builder. */
        builder = getBoundedBuilder(builder);

        /* Define the remaining string. */
        final String remainingString = builder.getRemaining();
        LogUtil.debug("remaining string = {}", remainingString);
        LogUtil.debug("remaining string length = {}", remainingString.length());

        /* Define the command string and its offset. */
        // List the command suggestions in the admin view.
        ServerCommandSource commandSource = CommandHelper.Source.getCommandSource(player).withLevel(4);

        // Initialize the command string.
        @NotNull String commandString = remainingString;

        // Replace the placeholder with vanilla Minecraft's target selector.
        commandString = commandString.replace("%player:name%", "@r");

        // Strip all leading blank characters.
        commandString = commandString.stripLeading();

        // Strip all trailing blank characters, but keep one if exists.
        commandString = CommandHelper.Parser.stripTrailingButKeepOne(commandString);
        LogUtil.debug("command string = {}", commandString);

        // Initialize the command string offset.
        final int commandStringOffset = commandString.length() - remainingString.length();
        LogUtil.debug("command string offset = {}", commandStringOffset);

        /* Make sub-builder. */
        @NotNull Suggestions commandSuggestions = CommandHelper.Suggestion.listSuggestions(commandSource, commandString);
        final int offset = commandSuggestions.getRange().getStart() - commandStringOffset;
        final int subBuilderStart = builder.getStart() + offset;
        final SuggestionsBuilder subBuilder = builder.createOffset(subBuilderStart);
        LogUtil.debug("subBuilder.getStart() = {}", subBuilder.getStart());

        commandSuggestions.getList().forEach(it -> subBuilder.suggest(it.getText()));

        /* Return sub-builder. */
        return subBuilder;
    }

    private @NotNull List<SuggestionsBuilder> makeSeparatorLiteralSuggestionsBuilders(@NotNull final String inputTrim, @NotNull final SuggestionsBuilder builder) {
        List<SuggestionsBuilder> subBuilders = new ArrayList<>();

        greedyStringSeparatorLiterals
            .forEach(separatorLiteral -> {
                /* Don't suggest if the suffix is the separator literal already. */
                if (inputTrim.endsWith(separatorLiteral)) {
                    return;
                }

                /* Make sub-builder. */
                final int subBuilderStart = builder.getInput().length();
                SuggestionsBuilder subBuilder = builder.createOffset(subBuilderStart);
                subBuilder.suggest(" " + separatorLiteral + " ");

                /* Merge builders. */
                subBuilders.add(subBuilder);
            });

        return subBuilders;
    }

}
