package io.github.sakurawald.fuji.core.command.suggestion.structure;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import lombok.Data;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class ComposedCommandSuggestionsProvider implements SuggestionProvider<ServerCommandSource> {

    final @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> argumentBuilder;
    final @Nullable SuggestionProvider<ServerCommandSource> delegate;
    final @NotNull BiConsumer<CommandContext<ServerCommandSource>, SuggestionsBuilder> runnable;

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        runnable.accept(context, builder);

        if (delegate == null) {
            // List the suggestions from the argument type, if no custom suggestions provider specified.
            return argumentBuilder.getType().listSuggestions(context,builder);
        }

        // List the suggestions from the custom suggestions provider.
        return this.delegate.getSuggestions(context, builder);
    }

}
