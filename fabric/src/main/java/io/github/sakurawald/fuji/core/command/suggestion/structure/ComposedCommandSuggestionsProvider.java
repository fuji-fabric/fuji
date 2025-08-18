package io.github.sakurawald.fuji.core.command.suggestion.structure;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.sakurawald.fuji.core.auxiliary.AsyncUtil;
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
    public @NotNull CompletableFuture<Suggestions> getSuggestions(@NotNull CommandContext<ServerCommandSource> context, @NotNull SuggestionsBuilder builder) throws CommandSyntaxException {
        /* Submit the command assistant task. */
        AsyncUtil.runAsyncAndHandleExceptions(() -> {
            this.runnable.accept(context, builder);
        });


        /* Create the command suggestions future. */
        if (this.delegate == null) {
            // List the suggestions from the argument type, if no custom suggestions provider specified.
            return this.argumentBuilder.getType().listSuggestions(context,builder);
        }

        // List the suggestions from the custom suggestions provider.
        return this.delegate.getSuggestions(context, builder);
    }

}
