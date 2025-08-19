package io.github.sakurawald.fuji.core.command.suggestion.structure;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.sakurawald.fuji.core.auxiliary.AsyncUtil;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import lombok.Data;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class ComposedCommandSuggestionsProvider implements SuggestionProvider<ServerCommandSource> {

    final @NotNull ArgumentType<?> argumentType;
    final @Nullable SuggestionProvider<ServerCommandSource> originalCustomSuggestionsProvider;
    final @NotNull BiConsumer<CommandContext<ServerCommandSource>, SuggestionsBuilder> onAskServerSideCommandSuggestionsHook;

    @TestCase(action = "Test the command suggestion functionality.", targets = {
        "Issue `/command-attachment attach-entity-one <uuid>` command, it should suggest the looking at entity UUID.",
        "Issue `/command-attachment attach-entity-one @e[type` command, it should be able to `insert` the suggestion content in the proper position. (non-zero-offset suggestions builder)",
        "Issue `/command-attachment attach-entity-one <uuid>` command, it should be able to `insert` the suggestion content in the proper position. (zero-offset suggestions builder)",
        "Issue `/command-attachment attach-block-one ` command, it should filter out the duplicated suggestions. (client-side suggestions and server-side suggestions)"
    })
    @Override
    public @NotNull CompletableFuture<Suggestions> getSuggestions(@NotNull CommandContext<ServerCommandSource> context, @NotNull SuggestionsBuilder builder) throws CommandSyntaxException {
        /* Run the onAskServerSideCommandSuggestions hook. */
        AsyncUtil.runAsyncAndHandleExceptions(() -> {
            this.onAskServerSideCommandSuggestionsHook.accept(context, builder);
        });

        /* Make the composed suggestions. */
        CompletableFuture<Suggestions> firstFuture = this.argumentType.listSuggestions(context, builder);
        CompletableFuture<Suggestions> secondFuture;
        if (this.originalCustomSuggestionsProvider != null) {
            secondFuture = this.originalCustomSuggestionsProvider.getSuggestions(context, builder);
        } else {
            secondFuture = Suggestions.empty();
        }

        return firstFuture
            .thenCombine(secondFuture, (firstValue, secondValue) -> {
                StringRange composedStringRange = composeStringRange(builder, firstValue, secondValue);

                List<Suggestion> firstSuggestionList = firstValue.getList();
                List<Suggestion> secondSuggestionList = secondValue.getList();

                // NOTE: Remove the duplicated entries from client-side suggestions and server-side suggestions.
                Set<Suggestion> uniqueSuggestions = new HashSet<>();
                uniqueSuggestions.addAll(firstSuggestionList);
                uniqueSuggestions.addAll(secondSuggestionList);

                return new Suggestions(composedStringRange, new ArrayList<>(uniqueSuggestions));
            });
    }

    private static @NotNull StringRange composeStringRange(@NotNull SuggestionsBuilder builder, @NotNull Suggestions first, @NotNull Suggestions second) {
        StringRange firstRange = first.getRange();
        StringRange secondRange = second.getRange();

        /* If the first suggestions takes effects, use the string range from it. */
        if (firstRange.getStart() != 0 && firstRange.getEnd() != 0) {
            return firstRange;
        }

        /* If the second suggestions takes effects, use the string range from it. */
        if (secondRange.getStart() != 0 && secondRange.getEnd() != 0) {
            return secondRange;
        }

        /* Use the fallback string range. */
        return StringRange.at(builder.getStart());
    }

}
